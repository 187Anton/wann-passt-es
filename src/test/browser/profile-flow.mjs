import assert from 'node:assert/strict';

// PLAYWRIGHT_MODULE can point to an existing local Playwright installation.
const playwrightModule = await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const { chromium } = playwrightModule.default ?? playwrightModule;
const [baseUrl, date] = process.argv.slice(2);
assert.match(baseUrl, /^http:\/\/localhost:\d+$/);
const browser = await chromium.launch({
  headless: process.env.DEMO_HEADED !== 'true',
  executablePath: process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE || undefined,
});
const name = 'Browser demo profile';
const renamed = 'Browser demo profile edited';

const horizontalOverflow = page => page.evaluate(
  () => document.documentElement.scrollWidth - document.documentElement.clientWidth);

try {
  const page = await browser.newPage();
  page.setDefaultTimeout(8000);
  await page.goto(`${baseUrl}/?startDate=${date}&endDate=${date}`);

  // The result block is the main area of the page and stays inside the
  // viewport at 1280 x 800, so the answer is visible without scrolling.
  await page.setViewportSize({ width: 1280, height: 800 });
  const emptyState = page.locator('.panel-empty');
  await emptyState.waitFor();
  const emptyBox = await emptyState.boundingBox();
  assert.ok(emptyBox.y + emptyBox.height <= 800,
    `empty state ends at ${Math.round(emptyBox.y + emptyBox.height)}px`);
  assert.ok(await horizontalOverflow(page) <= 0,
    'planning page scrolls horizontally at 1280px');

  await page.getByLabel(/Profilname/).fill(name);
  await page.getByRole('button', { name: 'Suchprofil speichern' }).click();
  await page.waitForURL(url => url.pathname.split(';')[0] === '/profiles');

  const entries = page.locator('.profile-entry');
  let entry = entries.filter({ hasText: name });
  await entry.waitFor();
  assert.equal(await entry.count(), 1);
  // The overview shows the stored location name instead of the raw id.
  assert.match(await entry.innerText(), /Demo town/);

  // Edit the profile: rename it and shorten the minimum duration.
  await entry.getByRole('link', { name: 'Bearbeiten' }).click();
  await page.waitForURL(url => /^\/profiles\/\d+\/edit$/.test(url.pathname));

  // The edit form works at both widths without horizontal scrolling.
  for (const [width, height] of [[360, 740], [1280, 800]]) {
    await page.setViewportSize({ width, height });
    assert.ok(await horizontalOverflow(page) <= 0,
      `edit page scrolls horizontally at ${width}px`);
  }
  await page.getByLabel('Profilname').fill(renamed);
  await page.getByLabel(/Mindestdauer/).fill('45');
  await page.getByRole('button', { name: 'Änderungen speichern' }).click();
  await page.waitForURL(url => url.pathname === '/profiles');

  entry = entries.filter({ hasText: renamed });
  await entry.waitFor();
  assert.match(await entry.innerText(), /45 Minuten/);

  // Reuse the profile in the planning form.
  await entry.getByRole('link', { name: 'Profil verwenden' }).click();
  await page.waitForURL(url => url.pathname === '/');
  assert.equal(await page.getByLabel('Aktivität').inputValue(), 'WALKING');
  assert.equal(await page.getByLabel(/Mindestdauer/).inputValue(), '45');

  // Deleting asks for confirmation first and keeps the profile when cancelled.
  await page.getByRole('link', { name: 'Suchprofile' }).click();
  await page.waitForURL(url => url.pathname === '/profiles');
  entry = entries.filter({ hasText: renamed });
  page.once('dialog', dialog => dialog.dismiss());
  await entry.getByRole('button', { name: 'Löschen' }).click();
  assert.equal(await entry.count(), 1);

  page.once('dialog', dialog => dialog.accept());
  await entry.getByRole('button', { name: 'Löschen' }).click();
  await entry.waitFor({ state: 'detached' });
  await page.reload();
  assert.equal(await entries.filter({ hasText: renamed }).count(), 0);
  console.log('PASS: save, edit, reuse, cancel deletion, confirm deletion, reload');

  // Core flows also work with JavaScript disabled.
  const noJs = await browser.newContext({ javaScriptEnabled: false });
  const plain = await noJs.newPage();
  plain.setDefaultTimeout(8000);
  const noJsName = 'Ohne JavaScript';
  await plain.goto(`${baseUrl}/?startDate=${date}&endDate=${date}`);
  await plain.getByLabel(/Profilname/).fill(noJsName);
  await plain.getByRole('button', { name: 'Suchprofil speichern' }).click();
  await plain.waitForURL(url => url.pathname.split(';')[0] === '/profiles');
  let plainEntry = plain.locator('.profile-entry').filter({ hasText: noJsName });
  await plainEntry.waitFor();
  assert.equal(await plainEntry.count(), 1);
  await plainEntry.getByRole('button', { name: 'Löschen' }).click();
  await plainEntry.waitFor({ state: 'detached' });
  assert.equal(await plain.locator('.profile-entry').filter({ hasText: noJsName }).count(), 0);
  await noJs.close();
  console.log('PASS: save and delete without JavaScript');

  // Every page has to work at 360 px and 1280 px wide without horizontal scrolling.
  for (const [width, height] of [[360, 740], [1280, 800]]) {
    const viewportPage = await browser.newPage({ viewport: { width, height } });
    for (const path of [`/?startDate=${date}&endDate=${date}`, '/locations?query=Ber',
      '/profiles', '/diese-seite-gibt-es-nicht']) {
      await viewportPage.goto(baseUrl + path);
      const overflow = await horizontalOverflow(viewportPage);
      assert.ok(overflow <= 0, `${path} scrolls horizontally by ${overflow}px at ${width}px`);
    }
    // The styled error page offers a way back to the planning page.
    assert.equal(await viewportPage.getByRole('link', { name: 'Zur Planung' }).count(), 1);
    await viewportPage.close();
  }
  console.log('PASS: no horizontal scrolling at 360px and 1280px on all pages');
} finally {
  await browser.close();
}
