import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Aegis AI | Agentic Business Operating System",
  description: "An enterprise-grade multi-agent operating system for orchestrating enterprise workflows, RAG, and memory.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="dark">
      <body className="min-h-screen font-sans antialiased overflow-x-hidden">
        {children}
      </body>
    </html>
  );
}
