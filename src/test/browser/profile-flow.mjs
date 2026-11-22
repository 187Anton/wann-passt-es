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
const profileCard = page => page.getByRole('listitem').filter({
  has: page.getByRole('heading', { name, exact: true }),
});
try {
  const page = await browser.newPage();
  page.setDefaultTimeout(8000);

  // Save the current selection as a profile. Weather wishes are radio buttons.
  await page.goto(`${baseUrl}/?startDate=${date}&endDate=${date}`);
  await page.getByRole('group', { name: 'Wind' }).getByRole('radio', { name: /mäßiger Wind/ }).check();
  await page.getByLabel(/Profilname/).fill(name);
  await page.getByRole('button', { name: 'Suchprofil speichern' }).click();
  await page.waitForURL(url => url.pathname.split(';')[0] === '/profiles');
  let card = profileCard(page);
  await card.waitFor();
  assert.equal(await card.count(), 1);
  // The location name is stored together with the profile.
  assert.match(await card.innerText(), /Demo town/);

  // Use the profile.
  await card.getByRole('link', { name: 'Profil verwenden' }).click();
  await page.waitForURL(url => url.pathname === '/');
  assert.equal(await page.getByLabel('Aktivität').inputValue(), 'WALKING');
  assert.equal(await page.getByLabel(/Mindestdauer/).inputValue(), '60');
  assert.ok(await page.getByRole('group', { name: 'Wind' })
    .getByRole('radio', { name: /mäßiger Wind/ }).isChecked());

  // Edit the profile.
  await page.getByRole('navigation').getByRole('link', { name: 'Suchprofile' }).click();
  card = profileCard(page);
  await card.getByRole('link', { name: 'Bearbeiten' }).click();
  await page.waitForURL(url => /\/profiles\/\d+\/edit$/.test(url.pathname));
  await page.getByLabel(/Mindestdauer/).fill('90');
  await page.getByRole('group', { name: 'Niederschlag' }).getByRole('radio', { name: /^Trocken/ }).check();
  await page.getByRole('button', { name: 'Änderungen speichern' }).click();
  await page.waitForURL(url => url.pathname.split(';')[0] === '/profiles');
  card = profileCard(page);
  assert.match(await card.innerText(), /90 Min\./);
  assert.match(await card.innerText(), /Trocken/);

  // Cancel the deletion first, then confirm it.
  page.once('dialog', dialog => dialog.dismiss());
  await card.getByRole('button', { name: 'Löschen' }).click();
  assert.equal(await card.count(), 1);

  page.once('dialog', dialog => dialog.accept());
  await card.getByRole('button', { name: 'Löschen' }).click();
  await card.waitFor({ state: 'detached' });
  await page.reload();
  assert.equal(await profileCard(page).count(), 0);
  console.log('PASS: save, reuse, edit, cancel deletion, confirm deletion, reload');
} finally {
  await browser.close();
}
