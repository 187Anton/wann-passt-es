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
try {
  const page = await browser.newPage();
  page.setDefaultTimeout(8000);
  await page.goto(`${baseUrl}/?searchDate=${date}`);
  await page.getByLabel(/Profilname/).fill(name);
  await page.getByRole('button', { name: 'Suchprofil speichern' }).click();
  await page.waitForURL(url => url.pathname.split(';')[0] === '/profiles');
  let row = page.getByRole('row').filter({ hasText: name });
  await row.waitFor();
  assert.equal(await row.count(), 1);

  await row.getByRole('link', { name: 'Profil verwenden' }).click();
  await page.waitForURL(url => url.pathname === '/');
  assert.equal(await page.getByLabel('Aktivität:').inputValue(), 'WALKING');
  assert.equal(await page.getByLabel(/Mindestens/).inputValue(), '60');

  await page.getByRole('link', { name: 'Gespeicherte Suchprofile' }).click();
  row = page.getByRole('row').filter({ hasText: name });
  page.once('dialog', dialog => dialog.dismiss());
  await row.getByRole('button', { name: 'Löschen' }).click();
  assert.equal(await row.count(), 1);

  page.once('dialog', dialog => dialog.accept());
  await row.getByRole('button', { name: 'Löschen' }).click();
  await row.waitFor({ state: 'detached' });
  await page.reload();
  assert.equal(await page.getByRole('row').filter({ hasText: name }).count(), 0);
  console.log('PASS: save, reuse, cancel deletion, confirm deletion, reload');
} finally {
  await browser.close();
}
