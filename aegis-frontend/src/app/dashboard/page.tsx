"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { api } from "@/lib/api";
import { 
  Activity, 
  BookOpen, 
  CheckCircle2, 
  Cpu, 
  Database, 
  ExternalLink,
  Layers, 
  LayoutDashboard, 
  Play, 
  RefreshCw, 
  Server, 
  Settings, 
  Terminal, 
  User,
  LogOut,
  ShieldCheck,
  ShieldAlert,
  Loader2
} from "lucide-react";

export default function Dashboard() {
  const router = useRouter();
  const [authenticated, setAuthenticated] = useState<boolean | null>(null);
  const [username, setUsername] = useState<string>("User");
  const [apiStatus, setApiStatus] = useState<"connecting" | "success" | "error">("connecting");
  const [apiMessage, setApiMessage] = useState<string>("Initializing verification...");
  const [logs, setLogs] = useState<Array<string>>([]);

  const addLog = (message: string) => {
    const timestamp = new Date().toISOString().split('T')[1].substring(0, 8);
    setLogs((prev) => [...prev, `[${timestamp}] ${message}`]);
  };

  useEffect(() => {
    const token = localStorage.getItem("aegis_token");
    const storedUsername = localStorage.getItem("aegis_username");

    if (!token) {
      router.push("/login");
      return;
    }

    if (storedUsername) {
      setUsername(storedUsername);
    }
    
    setAuthenticated(true);
    addLog("Client authorization token detected.");

    // Verify token against protected backend endpoint
    const verifyToken = async () => {
      try {
        setApiStatus("connecting");
        const response = await api.get("/v1/sample/protected");
        if (response.data.success) {
          setApiStatus("success");
          setApiMessage(response.data.data);
          addLog("INFO: JWT authorization verified with backend node.");
        } else {
          throw new Error("Invalid response format");
        }
      } catch (error: any) {
        console.error("Token verification failed:", error);
        setApiStatus("error");
        setApiMessage("Failed to authorize token against secure backend.");
        addLog("ERROR: Secure handshaking failed. Redirecting...");
        setTimeout(() => {
          handleLogout();
        }, 2000);
      }
    };

    verifyToken();
  }, [router]);

  const handleLogout = () => {
    localStorage.removeItem("aegis_token");
    localStorage.removeItem("aegis_username");
    router.push("/login");
  };

  if (authenticated === null) {
    return (
      <div className="min-h-screen bg-black flex flex-col items-center justify-center text-zinc-500 font-mono text-xs gap-3">
        <Loader2 className="h-4 w-4 animate-spin text-white" />
        Authenticating session...
      </div>
    );
  }

  return (
    <div className="flex min-h-screen bg-black font-sans text-zinc-300">
      
      {/* Sidebar - Notion / Linear style */}
      <aside className="hidden md:flex w-64 flex-col border-r border-zinc-900 bg-zinc-950/60 backdrop-blur-md">
        {/* Brand */}
        <div className="flex h-14 items-center gap-2 border-b border-zinc-900 px-6">
          <Layers className="h-4 w-4 text-white" />
          <span className="font-mono text-xs font-bold tracking-wider text-white">AEGIS OS</span>
        </div>

        {/* Navigation */}
        <nav className="flex-1 space-y-1 px-4 py-6">
          <Link href="/dashboard" className="flex items-center gap-3 rounded bg-zinc-900 px-3 py-2 text-xs font-medium text-white transition-all">
            <LayoutDashboard className="h-4 w-4" />
            Overview
          </Link>
          <button className="w-full flex items-center gap-3 rounded px-3 py-2 text-xs font-medium text-zinc-500 hover:text-zinc-300 hover:bg-zinc-900/30 transition-all cursor-not-allowed">
            <Cpu className="h-4 w-4" />
            Agent Registry <span className="ml-auto text-[9px] bg-zinc-900 px-1.5 py-0.5 rounded text-zinc-600">Sprint 3</span>
          </button>
          <button className="w-full flex items-center gap-3 rounded px-3 py-2 text-xs font-medium text-zinc-500 hover:text-zinc-300 hover:bg-zinc-900/30 transition-all cursor-not-allowed">
            <Activity className="h-4 w-4" />
            Workflows <span className="ml-auto text-[9px] bg-zinc-900 px-1.5 py-0.5 rounded text-zinc-600">Sprint 4</span>
          </button>
          <button className="w-full flex items-center gap-3 rounded px-3 py-2 text-xs font-medium text-zinc-500 hover:text-zinc-300 hover:bg-zinc-900/30 transition-all cursor-not-allowed">
            <BookOpen className="h-4 w-4" />
            RAG / Knowledge <span className="ml-auto text-[9px] bg-zinc-900 px-1.5 py-0.5 rounded text-zinc-600">Sprint 5</span>
          </button>
          <button className="w-full flex items-center gap-3 rounded px-3 py-2 text-xs font-medium text-zinc-500 hover:text-zinc-300 hover:bg-zinc-900/30 transition-all cursor-not-allowed">
            <Database className="h-4 w-4" />
            Memory Logs <span className="ml-auto text-[9px] bg-zinc-900 px-1.5 py-0.5 rounded text-zinc-600">Sprint 6</span>
          </button>
        </nav>

        {/* Footer info */}
        <div className="border-t border-zinc-900 p-4">
          <div className="flex items-center gap-2 px-2">
            <div className={`h-2 w-2 rounded-full ${apiStatus === "success" ? "bg-emerald-500" : "bg-red-500 animate-pulse"}`} />
            <span className="text-[10px] uppercase tracking-wider text-zinc-500 font-medium">
              {apiStatus === "success" ? "Backend Secure" : "Connecting..."}
            </span>
          </div>
        </div>
      </aside>

      {/* Main Area */}
      <div className="flex-1 flex flex-col">
        {/* Top Header */}
        <header className="flex h-14 items-center justify-between border-b border-zinc-900 px-6 md:px-8 bg-zinc-950/20">
          <div className="flex items-center gap-2 text-xs text-zinc-400">
            <span className="font-semibold text-zinc-300">Dashboard</span>
            <span>/</span>
            <span>Overview</span>
          </div>
          <div className="flex items-center gap-4">
            <Link href="/" className="inline-flex items-center gap-1 text-[11px] text-zinc-500 hover:text-zinc-300 transition-colors">
              Landing Page <ExternalLink className="h-3 w-3" />
            </Link>
            <div className="h-4 w-px bg-zinc-800" />
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-2 text-xs text-zinc-300">
                <div className="flex h-7 w-7 items-center justify-center rounded-full bg-zinc-900 border border-zinc-800">
                  <User className="h-3.5 w-3.5 text-zinc-400" />
                </div>
                <span className="hidden sm:inline font-medium">{username}</span>
              </div>
              <button 
                onClick={handleLogout}
                title="Log Out"
                className="p-1 rounded hover:bg-zinc-900 text-zinc-500 hover:text-zinc-300 transition-all"
              >
                <LogOut className="h-4 w-4" />
              </button>
            </div>
          </div>
        </header>

        {/* Main Content Grid */}
        <main className="flex-1 overflow-y-auto p-6 md:p-8 space-y-6">
          {/* Header Title */}
          <div>
            <h1 className="text-xl font-bold tracking-tight text-white font-sans">Business Operating System Overview</h1>
            <p className="text-xs text-zinc-500 mt-1">Real-time telemetry and status of your secure agent execution nodes.</p>
          </div>

          {/* Grid Stat Cards */}
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <div className="rounded border border-zinc-900 bg-zinc-950/40 p-5">
              <span className="text-[10px] font-semibold uppercase tracking-wider text-zinc-500">Active Agents</span>
              <div className="flex items-baseline gap-2 mt-2">
                <span className="text-2xl font-bold text-white font-mono">0</span>
                <span className="text-[10px] text-zinc-500">configured</span>
              </div>
            </div>
            <div className="rounded border border-zinc-900 bg-zinc-950/40 p-5">
              <span className="text-[10px] font-semibold uppercase tracking-wider text-zinc-500">Memory Nodes</span>
              <div className="flex items-baseline gap-2 mt-2">
                <span className="text-2xl font-bold text-white font-mono">0</span>
                <span className="text-[10px] text-zinc-500">stored vectors</span>
              </div>
            </div>
            <div className="rounded border border-zinc-900 bg-zinc-950/40 p-5">
              <span className="text-[10px] font-semibold uppercase tracking-wider text-zinc-500">Completed Workflows</span>
              <div className="flex items-baseline gap-2 mt-2">
                <span className="text-2xl font-bold text-white font-mono">0</span>
                <span className="text-[10px] text-zinc-500">runs</span>
              </div>
            </div>
            <div className="rounded border border-zinc-900 bg-zinc-950/40 p-5">
              <span className="text-[10px] font-semibold uppercase tracking-wider text-zinc-500">Security Access</span>
              <div className="flex items-baseline gap-2 mt-2">
                {apiStatus === "success" ? (
                  <span className="text-2xl font-bold text-emerald-500 font-mono flex items-center gap-1.5">
                    SECURE <ShieldCheck className="h-5 w-5 text-emerald-500" />
                  </span>
                ) : apiStatus === "connecting" ? (
                  <span className="text-xl font-bold text-zinc-500 font-mono">VERIFYING...</span>
                ) : (
                  <span className="text-2xl font-bold text-red-500 font-mono flex items-center gap-1.5">
                    ERROR <ShieldAlert className="h-5 w-5 text-red-500" />
                  </span>
                )}
              </div>
            </div>
          </div>

          {/* Secure Handshake Banner */}
          <div className={`p-4 rounded border flex items-center gap-3 ${
            apiStatus === "success" 
              ? "bg-emerald-950/10 border-emerald-900/40 text-emerald-400" 
              : apiStatus === "connecting" 
              ? "bg-zinc-950 border-zinc-900 text-zinc-400" 
              : "bg-red-950/10 border-red-900/40 text-red-400"
          }`}>
            {apiStatus === "connecting" && <Loader2 className="h-4 w-4 animate-spin" />}
            {apiStatus === "success" && <ShieldCheck className="h-5 w-5" />}
            {apiStatus === "error" && <ShieldAlert className="h-5 w-5" />}
            <div className="text-xs">
              <span className="font-semibold uppercase tracking-wider text-[10px] block mb-0.5">Secure Gateway Status</span>
              {apiMessage}
            </div>
          </div>

          {/* Main Workspace Panels */}
          <div className="grid gap-6 lg:grid-cols-3">
            
            {/* Agent Registry Panel */}
            <div className="lg:col-span-2 rounded border border-zinc-900 bg-zinc-950/30 p-6 space-y-4">
              <div className="flex items-center justify-between border-b border-zinc-900 pb-4">
                <div>
                  <h3 className="text-sm font-semibold text-white">Agent Node Registry</h3>
                  <p className="text-[11px] text-zinc-500 mt-0.5">Local executing agent profiles and their live status.</p>
                </div>
                <button className="inline-flex items-center gap-1.5 rounded border border-zinc-800 bg-zinc-900 px-2 py-1 text-[10px] text-zinc-400 cursor-not-allowed">
                  <RefreshCw className="h-3 w-3" /> Refresh
                </button>
              </div>

              {/* Empty state (Linear style) */}
              <div className="flex flex-col items-center justify-center py-16 text-center border border-dashed border-zinc-900 rounded bg-zinc-950/20">
                <Cpu className="h-8 w-8 text-zinc-800 mb-3" />
                <h4 className="text-xs font-semibold text-zinc-400">No agent nodes active</h4>
                <p className="text-[10px] text-zinc-600 max-w-xs mt-1">Configure and initialize agent logic in Spring Boot backend in Sprint 3.</p>
              </div>
            </div>

            {/* Terminal Activity Feed */}
            <div className="rounded border border-zinc-900 bg-zinc-950/40 p-6 flex flex-col h-[320px]">
              <div className="flex items-center justify-between border-b border-zinc-900 pb-4 mb-4">
                <div className="flex items-center gap-2">
                  <Terminal className="h-4 w-4 text-white" />
                  <span className="text-xs font-semibold text-white">Console Logs</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
                  <span className="text-[9px] uppercase tracking-wider text-zinc-500 font-medium font-mono">Telemetry Active</span>
                </div>
              </div>

              {/* Log messages */}
              <div className="flex-1 overflow-y-auto font-mono text-[10px] text-zinc-500 space-y-2.5 scrollable-list">
                <p className="text-zinc-600">[INFO] Aegis Web Console initialized.</p>
                {logs.map((log, index) => (
                  <p key={index} className="text-zinc-400">{log}</p>
                ))}
                <p className="text-zinc-600">[INFO] Layout stability configured (scrollbar-gutter stable).</p>
              </div>
            </div>

          </div>
        </main>
      </div>

    </div>
  );
}
