"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { KeyRound, Mail, Shield, User, Loader2 } from "lucide-react";

export default function LoginPage() {
  const router = useRouter();
  const [isLogin, setIsLogin] = useState(true);
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ type: "success" | "error"; text: String } | null>(null);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setLoading(true);
    setMessage(null);

    try {
      if (isLogin) {
        // Handle Login
        const response = await api.post("/v1/auth/login", { username, password });
        if (response.data.success) {
          const { accessToken } = response.data.data;
          localStorage.setItem("aegis_token", accessToken);
          localStorage.setItem("aegis_username", response.data.data.username);
          setMessage({ type: "success", text: "Login successful! Redirecting..." });
          setTimeout(() => {
            router.push("/dashboard");
          }, 1000);
        } else {
          setMessage({ type: "error", text: response.data.message || "Login failed" });
        }
      } else {
        // Handle Register
        const response = await api.post("/v1/auth/register", { username, email, password });
        if (response.data.success) {
          setMessage({ type: "success", text: "Registration successful! You can now log in." });
          setIsLogin(true);
          setEmail("");
          setPassword("");
        } else {
          setMessage({ type: "error", text: response.data.message || "Registration failed" });
        }
      }
    } catch (error: any) {
      console.error(error);
      const errorMsg = error.response?.data?.message || error.response?.data?.error || "An unexpected error occurred.";
      setMessage({ type: "error", text: errorMsg });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="relative min-h-screen bg-black grid-bg flex items-center justify-center p-6 selection:bg-zinc-800 selection:text-white">
      
      {/* Background glow effects */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-80 h-80 rounded-full bg-zinc-900/40 blur-[100px] pointer-events-none" />

      {/* Auth Card */}
      <div className="relative w-full max-w-md rounded-lg border border-zinc-900 bg-zinc-950/80 p-8 shadow-glow shadow-black/80 backdrop-blur-md">
        
        {/* Logo and title */}
        <div className="flex flex-col items-center text-center mb-8">
          <div className="flex h-10 w-10 items-center justify-center rounded border border-zinc-850 bg-zinc-900 mb-4 shadow-sm">
            <Shield className="h-5 w-5 text-white" />
          </div>
          <h2 className="text-xl font-semibold tracking-tight text-white">
            {isLogin ? "Sign in to Aegis OS" : "Create your Aegis account"}
          </h2>
          <p className="text-xs text-zinc-500 mt-1.5">
            {isLogin 
              ? "Access your agentic operating dashboard" 
              : "Register to begin configuring autonomous business flows"
            }
          </p>
        </div>

        {/* Message Alert */}
        {message && (
          <div className={`p-3 rounded text-xs mb-6 border ${
            message.type === "success" 
              ? "bg-emerald-950/30 text-emerald-400 border-emerald-900/60" 
              : "bg-red-950/30 text-red-400 border-red-900/60"
          }`}>
            {message.text}
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          
          {/* Username */}
          <div className="space-y-1.5">
            <label className="text-[11px] font-medium uppercase tracking-wider text-zinc-500" htmlFor="username">
              Username
            </label>
            <div className="relative flex items-center">
              <User className="absolute left-3 h-4 w-4 text-zinc-600" />
              <input
                id="username"
                type="text"
                required
                placeholder="nataraj"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full rounded border border-zinc-900 bg-zinc-950 py-2 pl-10 pr-4 text-xs text-white placeholder-zinc-700 outline-none hover:border-zinc-800 focus:border-zinc-600 focus:ring-1 focus:ring-zinc-600 transition-all"
              />
            </div>
          </div>

          {/* Email (only for Register) */}
          {!isLogin && (
            <div className="space-y-1.5">
              <label className="text-[11px] font-medium uppercase tracking-wider text-zinc-500" htmlFor="email">
                Email Address
              </label>
              <div className="relative flex items-center">
                <Mail className="absolute left-3 h-4 w-4 text-zinc-600" />
                <input
                  id="email"
                  type="email"
                  required
                  placeholder="natarajel.dev@gmail.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className="w-full rounded border border-zinc-900 bg-zinc-950 py-2 pl-10 pr-4 text-xs text-white placeholder-zinc-700 outline-none hover:border-zinc-800 focus:border-zinc-600 focus:ring-1 focus:ring-zinc-600 transition-all"
                />
              </div>
            </div>
          )}

          {/* Password */}
          <div className="space-y-1.5">
            <label className="text-[11px] font-medium uppercase tracking-wider text-zinc-500" htmlFor="password">
              Password
            </label>
            <div className="relative flex items-center">
              <KeyRound className="absolute left-3 h-4 w-4 text-zinc-600" />
              <input
                id="password"
                type="password"
                required
                placeholder="••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full rounded border border-zinc-900 bg-zinc-950 py-2 pl-10 pr-4 text-xs text-white placeholder-zinc-700 outline-none hover:border-zinc-800 focus:border-zinc-600 focus:ring-1 focus:ring-zinc-600 transition-all"
              />
            </div>
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={loading}
            className="w-full flex items-center justify-center gap-2 rounded bg-white py-2.5 text-xs font-semibold text-black hover:bg-zinc-200 transition-all disabled:opacity-50 disabled:hover:bg-white"
          >
            {loading ? (
              <Loader2 className="h-3 w-3 animate-spin text-black" />
            ) : isLogin ? (
              "Sign In"
            ) : (
              "Create Account"
            )}
          </button>
        </form>

        {/* Toggle link */}
        <div className="mt-6 text-center text-xs">
          <span className="text-zinc-500">
            {isLogin ? "Don't have an account?" : "Already have an account?"}
          </span>{" "}
          <button
            onClick={() => {
              setIsLogin(!isLogin);
              setMessage(null);
            }}
            className="font-medium text-white hover:underline ml-1"
          >
            {isLogin ? "Sign up" : "Log in"}
          </button>
        </div>

      </div>
    </div>
  );
}
