import assert from 'node:assert/strict';

// PLAYWRIGHT_MODULE can point to an existing local Playwright installation.
const { chromium } = await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const [baseUrl, date] = process.argv.slice(2);
assert.match(baseUrl, /^http:\/\/localhost:\d+$/);
const browser = await chromium.launch({
  headless: process.env.DEMO_HEADED !== 'true',
  executablePath: process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE || undefined,
});

const followingDate = new Date(Date.parse(date) + 86_400_000).toISOString().slice(0, 10);

async function assertNoOverflow(page) {
  const fits = await page.locator('html').evaluate(
    element => element.scrollWidth <= element.clientWidth,
  );
  assert.ok(fits, `Horizontal overflow on ${page.url()}`);
}

async function runScenario(javaScriptEnabled, width) {
  const page = await browser.newPage({ locale: 'de-DE', javaScriptEnabled, viewport: { width, height: 800 } });
  const name = javaScriptEnabled ? 'Browser demo profile' : 'No JavaScript demo profile';
  const editedName = `${name} updated`;
  const profileEntry = heading => page.getByRole('listitem').filter({
    has: page.getByRole('heading', { name: heading, exact: true }),
  });

  try {
    page.setDefaultTimeout(8000);
    const planningUrl = `${baseUrl}/?startDate=${date}&endDate=${followingDate}`;
    await page.goto(planningUrl);
    await assertNoOverflow(page);
    await page.keyboard.press('Tab');
    assert.equal(await page.locator(':focus-visible').textContent(), 'Zum Inhalt');

    const results = page.getByRole('region', { name: 'Passende Zeitfenster' });
    const window = results.getByRole('listitem').first();
    assert.match(await window.locator('time').nth(0).getAttribute('datetime'), new RegExp(`^${date}T22:00`));
    assert.match(await window.locator('time').nth(1).getAttribute('datetime'), new RegExp(`^${followingDate}T01:00`));
    assert.match(await window.textContent(), /180 Min\./);
    if (width === 1280) {
      const bounds = await window.boundingBox();
      assert.ok(bounds && bounds.y >= 0 && bounds.y + bounds.height <= 800);
    } else {
      await page.getByRole('link', { name: 'Suche anpassen' }).click();
      assert.equal(await page.locator(':focus').textContent(), 'Einstellungen');
    }
    await page.getByLabel('Aktivität').selectOption('CYCLING');
    await page.getByLabel('Mindestdauer (Min.)').fill('95');
    await page.getByLabel('Temperatur').selectOption('WARM');
    await page.getByLabel('Wind', { exact: true }).selectOption('MODERATE');
    await page.getByLabel('Niederschlag').selectOption('DRY');
    await page.getByRole('button', { name: 'Ort ändern' }).click();
    await page.getByLabel('Ortsname').fill('Testhafen');
    await page.getByRole('button', { name: 'Suchen', exact: true }).click();
    await assertNoOverflow(page);
    await page.getByRole('button', { name: 'Ort übernehmen: Testhafen' }).click();

    for (const [label, value] of [
      ['Aktivität', 'CYCLING'], ['Mindestdauer (Min.)', '95'],
      ['Temperatur', 'WARM'], ['Wind', 'MODERATE'], ['Niederschlag', 'DRY'],
      ['Startdatum', date], ['Enddatum', followingDate],
    ]) {
      assert.equal(await page.getByLabel(label, { exact: true }).inputValue(), value);
    }
    await page.getByLabel('Profilname').fill(name);
    await page.getByRole('button', { name: 'Suchprofil speichern' }).click();
    await page.waitForURL(url => url.pathname.split(';')[0] === '/profiles');
    await profileEntry(name).waitFor();
    await assertNoOverflow(page);

    await profileEntry(name).getByRole('link', { name: 'Bearbeiten' }).click();
    await assertNoOverflow(page);
    await page.getByLabel('Profilname').fill(editedName);
    await page.getByLabel('Mindestdauer (Min.)').fill('120');
    await page.getByLabel('Anderen Ort suchen').fill('Testberg');
    await page.getByRole('button', { name: 'Ort suchen', exact: true }).click();
    await page.getByRole('radio', { name: /Testberg/ }).check();
    await page.getByRole('button', { name: 'Änderungen speichern' }).click();
    await profileEntry(editedName).waitFor();
    assert.match(await profileEntry(editedName).textContent(), /Testberg/);
    assert.match(await page.getByRole('status').textContent(), /Änderungen wurden gespeichert/);

    await profileEntry(editedName).getByRole('link', { name: 'Profil verwenden' }).click();
    await page.waitForURL(url => url.pathname === '/');
    assert.equal(await page.getByLabel('Aktivität').inputValue(), 'CYCLING');
    assert.equal(await page.getByLabel('Mindestdauer (Min.)').inputValue(), '120');

    await page.getByRole('link', { name: 'Suchprofile' }).click();
    const entry = profileEntry(editedName);
    await entry.waitFor();
    if (javaScriptEnabled) {
      page.once('dialog', dialog => dialog.dismiss());
      await entry.getByRole('button', { name: 'Löschen' }).click();
      assert.equal(await entry.count(), 1);
      page.once('dialog', dialog => dialog.accept());
    }
    await entry.getByRole('button', { name: 'Löschen' }).click();
    await entry.waitFor({ state: 'detached' });
    await page.reload();
    assert.equal(await entry.count(), 0);

    const errorPage = await page.goto(`${baseUrl}/gibt-es-nicht`);
    assert.equal(errorPage.status(), 404);
    await page.getByRole('heading', { name: 'Seite nicht gefunden' }).waitFor();
    await assertNoOverflow(page);
  } finally {
    await page.close();
  }
}

try {
  for (const width of [360, 1280]) {
    await runScenario(true, width);
    await runScenario(false, width);
  }
  console.log('PASS: profile and location flow at desktop and mobile widths, with and without JavaScript');
} finally {
  await browser.close();
}
