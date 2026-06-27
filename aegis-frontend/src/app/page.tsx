import Link from "next/link";
import { ArrowRight, Cpu, Database, GitBranch, Layers, Play, Shield, Terminal } from "lucide-react";

export default function Home() {
  return (
    <div className="relative min-h-screen bg-black grid-bg selection:bg-zinc-800 selection:text-white">
      {/* Navigation */}
      <header className="sticky top-0 z-50 w-full border-b border-zinc-800/80 bg-black/80 backdrop-blur-md">
        <div className="mx-auto flex h-14 max-w-7xl items-center justify-between px-6">
          <div className="flex items-center gap-2">
            <Layers className="h-5 w-5 text-white" />
            <span className="font-mono text-sm font-semibold tracking-wider text-white">AEGIS AI</span>
          </div>
          <nav className="hidden md:flex items-center gap-6">
            <Link href="#features" className="text-xs text-zinc-400 hover:text-white transition-colors">Features</Link>
            <Link href="#architecture" className="text-xs text-zinc-400 hover:text-white transition-colors">Architecture</Link>
            <Link href="https://github.com/Nataraj-EL/aegis-ai" target="_blank" className="text-xs text-zinc-400 hover:text-white transition-colors">GitHub</Link>
          </nav>
          <div className="flex items-center gap-3">
            <Link href="/dashboard" className="rounded border border-zinc-800 bg-zinc-900 px-3 py-1.5 text-xs font-medium text-zinc-300 hover:bg-zinc-800 hover:text-white transition-all">
              Dashboard Demo
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="mx-auto max-w-7xl px-6 pt-20 pb-16">
        <div className="flex flex-col items-center text-center">
          {/* Badge */}
          <div className="inline-flex items-center gap-1.5 rounded-full border border-zinc-800 bg-zinc-900/60 px-3 py-1 text-xs text-zinc-400 backdrop-blur-sm">
            <Shield className="h-3 w-3 text-emerald-500" />
            <span>Sprint 1 Base Deployable</span>
          </div>

          {/* Heading */}
          <h1 className="mt-8 max-w-4xl text-4xl font-semibold tracking-tight text-white sm:text-6xl md:text-7xl font-sans">
            The Agentic Operating System for <span className="text-transparent bg-clip-text bg-gradient-to-r from-zinc-100 via-zinc-400 to-zinc-600">Enterprise Operations.</span>
          </h1>

          {/* Subtitle */}
          <p className="mt-6 max-w-2xl text-base text-zinc-400 sm:text-lg leading-relaxed">
            Orchestrate autonomous agent systems, synchronize cognitive enterprise memory, and execute RAG pipelines with mathematical accuracy. Clean architecture. Scalable design.
          </p>

          {/* Action Buttons */}
          <div className="mt-10 flex flex-wrap items-center justify-center gap-4">
            <Link href="/dashboard" className="inline-flex items-center gap-2 rounded bg-white px-5 py-2.5 text-xs font-semibold text-black hover:bg-zinc-200 transition-all shadow-glow shadow-white/5">
              Launch Agent Dashboard <Play className="h-3 w-3 fill-black text-black" />
            </Link>
            <Link href="#architecture" className="inline-flex items-center gap-2 rounded border border-zinc-800 bg-zinc-950 px-5 py-2.5 text-xs font-semibold text-zinc-400 hover:text-white hover:border-zinc-700 transition-all">
              Explore Architecture <ArrowRight className="h-3 w-3" />
            </Link>
          </div>
        </div>

        {/* Feature Grid */}
        <section id="features" className="mt-32">
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {/* Feature 1 */}
            <div className="group relative rounded border border-zinc-800/80 bg-zinc-950/60 p-6 backdrop-blur-sm hover:border-zinc-700 transition-all duration-300">
              <div className="inline-flex items-center justify-center rounded bg-zinc-900 p-2.5 text-white border border-zinc-800">
                <Cpu className="h-5 w-5" />
              </div>
              <h3 className="mt-4 text-sm font-semibold text-white">Agentic Orchestration</h3>
              <p className="mt-2 text-xs leading-relaxed text-zinc-400">
                Define and run multi-agent architectures using structured tool executions, workflow pipelines, and custom state machines.
              </p>
            </div>

            {/* Feature 2 */}
            <div className="group relative rounded border border-zinc-800/80 bg-zinc-950/60 p-6 backdrop-blur-sm hover:border-zinc-700 transition-all duration-300">
              <div className="inline-flex items-center justify-center rounded bg-zinc-900 p-2.5 text-white border border-zinc-800">
                <Database className="h-5 w-5" />
              </div>
              <h3 className="mt-4 text-sm font-semibold text-white">Enterprise Cognitive Memory</h3>
              <p className="mt-2 text-xs leading-relaxed text-zinc-400">
                Maintain continuous contextual state across sessions with specialized postgres-backed episodic and semantic memories.
              </p>
            </div>

            {/* Feature 3 */}
            <div className="group relative rounded border border-zinc-800/80 bg-zinc-950/60 p-6 backdrop-blur-sm hover:border-zinc-700 transition-all duration-300">
              <div className="inline-flex items-center justify-center rounded bg-zinc-900 p-2.5 text-white border border-zinc-800">
                <GitBranch className="h-5 w-5" />
              </div>
              <h3 className="mt-4 text-sm font-semibold text-white">Dynamic RAG Pipelines</h3>
              <p className="mt-2 text-xs leading-relaxed text-zinc-400">
                Retrieve documentation and source text via native PostgreSQL pgvector lookups matched with Google Gemini Embeddings.
              </p>
            </div>
          </div>
        </section>

        {/* Scaffold Highlights / Architecture */}
        <section id="architecture" className="mt-32 rounded border border-zinc-800/80 bg-zinc-950/40 p-8 backdrop-blur-sm">
          <div className="grid gap-8 lg:grid-cols-2 items-center">
            <div>
              <div className="inline-flex items-center gap-1.5 rounded-full border border-zinc-800/80 bg-zinc-900/40 px-2.5 py-0.5 text-[10px] uppercase tracking-wider text-zinc-400">
                <Terminal className="h-3 w-3 text-white" />
                <span>Clean Architecture Structure</span>
              </div>
              <h2 className="mt-4 text-2xl font-bold tracking-tight text-white sm:text-3xl">
                Engineered for strict enterprise compliance.
              </h2>
              <p className="mt-4 text-xs leading-relaxed text-zinc-400">
                Aegis AI decouples model providers, vector stores, and cognitive orchestration logic into modular backend packages and clean state interfaces. No boilerplate, no shortcuts.
              </p>
              <div className="mt-6 space-y-3">
                <div className="flex items-start gap-3">
                  <div className="mt-0.5 h-1.5 w-1.5 rounded-full bg-white" />
                  <p className="text-xs text-zinc-400"><strong>Backend Packages</strong> initialized: agent, ai, workflow, rag, memory, tool, security, exception</p>
                </div>
                <div className="flex items-start gap-3">
                  <div className="mt-0.5 h-1.5 w-1.5 rounded-full bg-white" />
                  <p className="text-xs text-zinc-400"><strong>Spring Boot 3.5.x</strong> integration ready with dynamic environment configuration</p>
                </div>
                <div className="flex items-start gap-3">
                  <div className="mt-0.5 h-1.5 w-1.5 rounded-full bg-white" />
                  <p className="text-xs text-zinc-400"><strong>Next.js 15 & Tailwind</strong> modern dark enterprise UI styled after Linear & Vercel</p>
                </div>
              </div>
            </div>

            <div className="rounded border border-zinc-800 bg-zinc-950 p-6 font-mono text-[11px] text-zinc-400 shadow-glow shadow-zinc-950">
              <div className="flex items-center justify-between border-b border-zinc-800 pb-3 mb-4">
                <div className="flex gap-1.5">
                  <div className="h-2.5 w-2.5 rounded-full bg-red-500/80" />
                  <div className="h-2.5 w-2.5 rounded-full bg-yellow-500/80" />
                  <div className="h-2.5 w-2.5 rounded-full bg-green-500/80" />
                </div>
                <span className="text-zinc-500">aegis-backend-structure</span>
              </div>
              <p className="text-emerald-500">// com.aegis.backend packages</p>
              <p className="mt-1">├── <span className="text-white">agent/</span> <span className="text-zinc-600">// Agent definition & state</span></p>
              <p>├── <span className="text-white">ai/</span> <span className="text-zinc-600">// LLM clients & Embeddings</span></p>
              <p>├── <span className="text-white">workflow/</span> <span className="text-zinc-600">// Multi-agent coordination</span></p>
              <p>├── <span className="text-white">rag/</span> <span className="text-zinc-600">// Vector index & search</span></p>
              <p>├── <span className="text-white">memory/</span> <span className="text-zinc-600">// Context & storage</span></p>
              <p>├── <span className="text-white">tool/</span> <span className="text-zinc-600">// Custom task tools</span></p>
              <p>├── <span className="text-white">security/</span> <span className="text-zinc-600">// JWT stateless auth</span></p>
              <p>├── <span className="text-white">config/</span> <span className="text-zinc-600">// Property files</span></p>
              <p>└── <span className="text-white">exception/</span> <span className="text-zinc-600">// Exception handler</span></p>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="mx-auto max-w-7xl border-t border-zinc-900 px-6 py-8 text-center text-xs text-zinc-500">
        <p>© 2026 Aegis AI. All rights reserved. Designed for portfolios demanding absolute code quality.</p>
      </footer>
    </div>
  );
}
