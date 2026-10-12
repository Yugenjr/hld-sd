"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Zap, MapPin, Calendar, History, Activity, LogOut, User as UserIcon } from "lucide-react";
import { User } from "../types";
import { api } from "../lib/api";

export function Navbar() {
  const pathname = usePathname();
  const router = useRouter();
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    api.getMe().then(setUser).catch(() => {
      // not logged in
    });
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("voltflow_token");
    localStorage.removeItem("voltflow_user");
    router.push("/auth");
  };

  const navLinks = [
    { href: "/dashboard", label: "Dashboard", icon: Activity },
    { href: "/stations", label: "Find Stations", icon: MapPin },
    { href: "/reservations", label: "Reservations", icon: Calendar },
    { href: "/history", label: "Charging History", icon: History },
  ];

  return (
    <header className="sticky top-0 z-50 glass-panel border-b border-slate-800 bg-slate-950/80 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        {/* Brand */}
        <Link href="/dashboard" className="flex items-center space-x-3 group">
          <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-600 to-teal-400 flex items-center justify-center shadow-lg shadow-emerald-500/20 group-hover:shadow-emerald-500/40 transition-all">
            <Zap className="w-6 h-6 text-slate-950 fill-current" />
          </div>
          <div>
            <div className="font-bold text-lg tracking-tight bg-gradient-to-r from-white via-slate-200 to-emerald-400 bg-clip-text text-transparent">
              VoltFlow
            </div>
            <div className="text-[10px] text-emerald-400 font-medium tracking-wider uppercase">
              Smart EV Platform
            </div>
          </div>
        </Link>

        {/* Navigation links */}
        <nav className="hidden md:flex items-center space-x-1">
          {navLinks.map((link) => {
            const Icon = link.icon;
            const isActive = pathname === link.href || (link.href !== "/dashboard" && pathname.startsWith(link.href));
            return (
              <Link
                key={link.href}
                href={link.href}
                className={`flex items-center space-x-2 px-3 py-2 rounded-lg text-sm font-medium transition-all ${
                  isActive
                    ? "bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 shadow-sm"
                    : "text-slate-400 hover:text-slate-200 hover:bg-slate-800/50"
                }`}
              >
                <Icon className={`w-4 h-4 ${isActive ? "text-emerald-400" : "text-slate-400"}`} />
                <span>{link.label}</span>
              </Link>
            );
          })}
        </nav>

        {/* User profile & actions */}
        <div className="flex items-center space-x-4">
          {user && (
            <div className="flex items-center space-x-3 bg-slate-900/60 border border-slate-800 rounded-full px-3 py-1.5">
              <div className="w-7 h-7 rounded-full bg-emerald-500/20 border border-emerald-500/30 flex items-center justify-center text-emerald-400 text-xs font-semibold">
                {user.name.charAt(0)}
              </div>
              <div className="hidden sm:block text-left text-xs">
                <div className="font-medium text-slate-200">{user.name}</div>
                <div className="text-[10px] text-slate-400">{user.email}</div>
              </div>
            </div>
          )}

          <button
            onClick={handleLogout}
            title="Log Out"
            className="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors border border-transparent hover:border-rose-500/20"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </header>
  );
}
