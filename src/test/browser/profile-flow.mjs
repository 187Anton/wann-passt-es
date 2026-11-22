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
  const profileCard = heading => page.getByRole('listitem').filter({
    has: page.getByRole('heading', { name: heading, exact: true }),
  });

  try {
    page.setDefaultTimeout(8000);
    await page.goto(`${baseUrl}/?startDate=${date}&endDate=${followingDate}`);
    await assertNoOverflow(page);
    await page.keyboard.press('Tab');
    assert.equal(await page.locator(':focus-visible').textContent(), 'Zum Inhalt');
    const results = page.getByRole('region', { name: 'Passende Zeitfenster' });
    assert.equal(await results.locator('.timeline-day').count(), 2);
    assert.equal(await results.locator('.time-band').count(), 2);
    const window = results.locator('details');
    assert.equal(await window.count(), 1);
    assert.match(await window.getByText('3 Std.', { exact: false }).textContent(), /3 Std\./);
    await window.locator('summary').click();
    assert.ok(await window.getByText('22 °C', { exact: true }).isVisible());
    if (width === 360) {
      await page.getByRole('link', { name: 'Suche anpassen' }).click();
      assert.equal(await page.locator(':focus').getAttribute('id'), 'planning-form');
    }
    await page.getByLabel('Aktivität').selectOption('CYCLING');
    await page.getByLabel('Mindestdauer in Minuten').fill('95');
    const choices = [
      ['Temperatur', /Warm.*20–30 °C/],
      ['Wind', /Höchstens mäßiger Wind.*bis 30 km\/h/],
      ['Niederschlag', /Trocken.*bis 0,0 mm\/h/],
    ];
    const radio = (group, name) => page.getByRole('group', { name: group, exact: true }).getByRole('radio', { name });
    for (const [group, name] of choices) {
      await radio(group, name).check();
    }
    await page.getByRole('button', { name: 'Ort ändern' }).click();
    await page.getByLabel('Ortsname').fill('Testhafen');
    await page.getByRole('button', { name: 'Suchen', exact: true }).click();
    await assertNoOverflow(page);
    await page.getByRole('button', { name: 'Ort übernehmen: Testhafen' }).click();

    for (const [label, value] of [
      ['Aktivität', 'CYCLING'], ['Mindestdauer in Minuten', '95'],
      ['Startdatum', date], ['Enddatum', followingDate],
    ]) {
      assert.equal(await page.getByLabel(label, { exact: true }).inputValue(), value);
    }
    for (const [group, name] of choices) {
      assert.ok(await radio(group, name).isChecked());
    }
    await page.getByLabel('Profilname').fill(name);
    await page.getByRole('button', { name: 'Suchprofil speichern' }).click();
    await page.waitForURL(url => url.pathname.split(';')[0] === '/profiles');
    await profileCard(name).waitFor();
    await assertNoOverflow(page);

    await profileCard(name).getByRole('link', { name: 'Bearbeiten' }).click();
    await assertNoOverflow(page);
    await page.getByLabel('Profilname').fill(editedName);
    await page.getByLabel('Mindestdauer in Minuten').fill('120');
    await page.getByLabel('Anderen Ort suchen').fill('Testberg');
    await page.getByRole('button', { name: 'Ort suchen', exact: true }).click();
    await page.getByRole('radio', { name: /Testberg/ }).check();
    await page.getByRole('button', { name: 'Änderungen speichern' }).click();
    await profileCard(editedName).waitFor();
    assert.match(await profileCard(editedName).textContent(), /Testberg/);
    assert.match(await page.getByRole('status').textContent(), /Änderungen wurden gespeichert/);

    await profileCard(editedName).getByRole('link', { name: 'Profil verwenden' }).click();
    await page.waitForURL(url => url.pathname === '/');
    assert.equal(await page.getByLabel('Aktivität').inputValue(), 'CYCLING');
    assert.equal(await page.getByLabel('Mindestdauer in Minuten').inputValue(), '120');

    await page.getByRole('link', { name: 'Suchprofile' }).click();
    const card = profileCard(editedName);
    await card.waitFor();
    if (javaScriptEnabled) {
      page.once('dialog', dialog => dialog.dismiss());
      await card.getByRole('button', { name: 'Löschen' }).click();
      assert.equal(await card.count(), 1);
      page.once('dialog', dialog => dialog.accept());
    }
    await card.getByRole('button', { name: 'Löschen' }).click();
    await card.waitFor({ state: 'detached' });
    await page.reload();
    assert.equal(await card.count(), 0);

    const errorPage = await page.goto(`${baseUrl}/gibt-es-nicht`);
    assert.equal(errorPage.status(), 404);
    await page.getByRole('heading', { name: 'Seite nicht gefunden' }).waitFor();
    await assertNoOverflow(page);
  } finally {
    await page.close();
  }
}

try {
  await runScenario(true, 1280);
  await runScenario(false, 360);
  console.log('PASS: profile and location flow at desktop and mobile widths, with and without JavaScript');
} finally {
  await browser.close();
}
