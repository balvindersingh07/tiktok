import { defineConfig, type Plugin } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import { getRequestListener } from "@hono/node-server";

function honoBackendPlugin(): Plugin {
  return {
    name: "tashan-hono-backend",
    async configureServer(server) {
      process.env.VITE_EMBEDDED = "true";
      const { app, initializeBackend } = await import("./server/src/server.ts");
      await initializeBackend();
      const listener = getRequestListener(app.fetch);

      server.middlewares.use((req, res, next) => {
        const url = req.url || "";
        if (
          url.startsWith("/api") ||
          url.startsWith("/health") ||
          url.startsWith("/storage")
        ) {
          return listener(req, res);
        }
        next();
      });
    },
    async configurePreviewServer(server) {
      process.env.VITE_EMBEDDED = "true";
      const { app, initializeBackend } = await import("./server/src/server.ts");
      await initializeBackend();
      const listener = getRequestListener(app.fetch);

      server.middlewares.use((req, res, next) => {
        const url = req.url || "";
        if (
          url.startsWith("/api") ||
          url.startsWith("/health") ||
          url.startsWith("/storage")
        ) {
          return listener(req, res);
        }
        next();
      });
    },
  };
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss(), honoBackendPlugin()],
  server: {
    host: "0.0.0.0",
    port: 3000,
    strictPort: true,
  },
  preview: {
    host: "0.0.0.0",
    port: 3000,
    strictPort: true,
  },
});
