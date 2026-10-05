import { defineConfig } from '@playwright/test'

const mvnw = process.platform === 'win32' ? '.\\mvnw.cmd' : './mvnw'

export default defineConfig({
  testDir: './tests',
  workers: 1,
  use: { baseURL: 'http://127.0.0.1:5173' },
  webServer: [
    {
      command: `${mvnw} spring-boot:run`,
      cwd: '../backend',
      url: 'http://127.0.0.1:8080/api/slots',
      reuseExistingServer: !process.env.CI,
      timeout: 180_000,
    },
    {
      command: 'npm run dev -- --host 127.0.0.1 --port 5173 --strictPort',
      url: 'http://127.0.0.1:5173',
      reuseExistingServer: !process.env.CI,
      timeout: 60_000,
    },
  ],
})
