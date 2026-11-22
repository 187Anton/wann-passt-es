import assert from 'node:assert/strict';

// PLAYWRIGHT_MODULE can point to an existing local Playwright installation.
const { chromium } = await import(process.env.PLAYWRIGHT_MODULE || 'playwright');
const [baseUrl, date] = process.argv.slice(2);
assert.match(baseUrl, /^http:\/\/localhost:\d+$/);
const browser = await chromium.launch({
  headless: process.env.DEMO_HEADED !== 'true',
  executablePath: process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE || undefined,
});
const name = 'Browser demo profile';
const renamed = 'Browser demo profile edited';
try {
  const page = await browser.newPage();
  page.setDefaultTimeout(8000);
  await page.goto(`${baseUrl}/?startDate=${date}&endDate=${date}`);
  await page.getByLabel(/Profilname/).fill(name);
  await page.getByRole('button', { name: 'Suchprofil speichern' }).click();
  await page.waitForURL(url => url.pathname.split(';')[0] === '/profiles');

  const rows = page.locator('.profile-row');
  let row = rows.filter({ hasText: name });
  await row.waitFor();
  assert.equal(await row.count(), 1);
  // The overview shows the stored location name instead of the raw id.
  assert.match(await row.innerText(), /Demo town/);

  // Edit the profile: rename it and shorten the minimum duration.
  await row.getByRole('link', { name: 'Bearbeiten' }).click();
  await page.waitForURL(url => /^\/profiles\/\d+\/edit$/.test(url.pathname));
  await page.getByLabel('Profilname').fill(renamed);
  await page.getByLabel(/Mindestdauer/).fill('45');
  await page.getByRole('button', { name: 'Änderungen speichern' }).click();
  await page.waitForURL(url => url.pathname === '/profiles');

  row = rows.filter({ hasText: renamed });
  await row.waitFor();
  assert.match(await row.innerText(), /45 Min\./);

  // Reuse the profile in the planning form.
  await row.getByRole('link', { name: 'Profil verwenden' }).click();
  await page.waitForURL(url => url.pathname === '/');
  assert.equal(await page.getByLabel('Aktivität').inputValue(), 'WALKING');
  assert.equal(await page.getByLabel(/Mindestdauer/).inputValue(), '45');

  // Deleting asks for confirmation first and keeps the profile when cancelled.
  await page.getByRole('link', { name: 'Suchprofile' }).click();
  await page.waitForURL(url => url.pathname === '/profiles');
  row = rows.filter({ hasText: renamed });
  page.once('dialog', dialog => dialog.dismiss());
  await row.getByRole('button', { name: 'Löschen' }).click();
  assert.equal(await row.count(), 1);

  page.once('dialog', dialog => dialog.accept());
  await row.getByRole('button', { name: 'Löschen' }).click();
  await row.waitFor({ state: 'detached' });
  await page.reload();
  assert.equal(await rows.filter({ hasText: renamed }).count(), 0);
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
  let plainRow = plain.locator('.profile-row').filter({ hasText: noJsName });
  await plainRow.waitFor();
  assert.equal(await plainRow.count(), 1);
  await plainRow.getByRole('button', { name: 'Löschen' }).click();
  await plainRow.waitFor({ state: 'detached' });
  assert.equal(await plain.locator('.profile-row').filter({ hasText: noJsName }).count(), 0);
  await noJs.close();
  console.log('PASS: save and delete without JavaScript');

  // Every page has to work at a 360 px wide viewport without horizontal scrolling.
  const mobile = await browser.newPage({ viewport: { width: 360, height: 740 } });
  for (const path of [`/?startDate=${date}&endDate=${date}`, '/locations?query=Ber', '/profiles',
    '/diese-seite-gibt-es-nicht']) {
    await mobile.goto(baseUrl + path);
    const overflow = await mobile.evaluate(
      () => document.documentElement.scrollWidth - document.documentElement.clientWidth);
    assert.ok(overflow <= 0, `${path} scrolls horizontally by ${overflow}px at 360px`);
  }
  console.log('PASS: no horizontal scrolling at 360px on all pages');

  // The first result or the empty state has to be visible without scrolling.
  const desktop = await browser.newPage({ viewport: { width: 1280, height: 800 } });
  await desktop.goto(`${baseUrl}/?startDate=${date}&endDate=${date}`);
  const outcome = desktop.locator('.day, .state, .alert--error').first();
  const box = await outcome.boundingBox();
  assert.ok(box && box.y + box.height <= 800,
    `first result is below the fold at 1280x800 (bottom ${box?.y + box?.height})`);
  const desktopOverflow = await desktop.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth);
  assert.ok(desktopOverflow <= 0, `planning scrolls horizontally by ${desktopOverflow}px at 1280px`);
  console.log('PASS: first result visible and no horizontal scrolling at 1280x800');
} finally {
  await browser.close();
}
