import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "VoltFlow — Smart EV Charging Platform",
  description: "Next-generation electric vehicle charging station reservation, live telemetry, and energy management platform.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark">
      <body className="bg-slate-950 text-slate-100 min-h-screen">
        {children}
      </body>
    </html>
  );
}
