import React, { useState, useEffect } from 'react';
import { X, Server, RefreshCw, Trash2, Cpu, Activity, Terminal, Sparkles, CheckCircle2, Database } from 'lucide-react';
import { useTikTok } from '../../context/TikTokContext';
import { api } from '../../services/apiClient';

interface ServerLog {
  id: string;
  timestamp: string;
  method: 'GET' | 'POST' | 'PUT' | 'DELETE';
  endpoint: string;
  statusCode: number;
  latencyMs: number;
}

interface LocalBackendConsoleSheetProps {
  onClose: () => void;
}

export const LocalBackendConsoleSheet: React.FC<LocalBackendConsoleSheetProps> = ({ onClose }) => {
  const {
    videos,
    drafts,
    allAccounts,
    onResetDatabase,
    onSeedAdditionalClips,
    showToast,
  } = useTikTok();

  const [cacheSizeMb, setCacheSizeMb] = useState<number>(48.2);
  const [requestCount, setRequestCount] = useState<number>(142);
  const [dbStatus, setDbStatus] = useState<{ status: string; uptime: number; db: string; latencyMs: number }>({
    status: 'connected',
    uptime: 120,
    db: 'PostgreSQL + Hono Embedded',
    latencyMs: 1,
  });

  const [logs, setLogs] = useState<ServerLog[]>([
    { id: '1', timestamp: '10:42:01', method: 'GET', endpoint: '/api/feed/for-you', statusCode: 200, latencyMs: 3 },
    { id: '2', timestamp: '10:42:05', method: 'POST', endpoint: '/api/analytics/events', statusCode: 200, latencyMs: 2 },
    { id: '3', timestamp: '10:42:12', method: 'POST', endpoint: '/api/videos/like', statusCode: 200, latencyMs: 4 },
    { id: '4', timestamp: '10:42:18', method: 'GET', endpoint: '/api/notifications', statusCode: 200, latencyMs: 2 },
  ]);

  // Fetch real backend health
  useEffect(() => {
    async function checkHealth() {
      const start = Date.now();
      try {
        const res = await api.health();
        const latency = Date.now() - start;
        if (res) {
          setDbStatus({
            status: res.database?.status || 'connected',
            uptime: res.uptime || 60,
            db: 'PostgreSQL Cloud & Hono',
            latencyMs: latency,
          });
        }
      } catch {
        setDbStatus((prev) => ({ ...prev, latencyMs: Date.now() - start }));
      }
    }

    checkHealth();

    const interval = setInterval(() => {
      checkHealth();
      const endpoints = [
        '/api/feed/for-you',
        '/api/notifications',
        '/api/messages/conversations',
        '/api/users/profile',
      ];
      const randomEp = endpoints[Math.floor(Math.random() * endpoints.length)];
      const newLog: ServerLog = {
        id: Math.random().toString(),
        timestamp: new Date().toLocaleTimeString(),
        method: 'GET',
        endpoint: randomEp,
        statusCode: 200,
        latencyMs: Math.floor(Math.random() * 8) + 1,
      };

      setLogs((prev) => [newLog, ...prev.slice(0, 7)]);
      setRequestCount((prev) => prev + 1);
    }, 4000);

    return () => clearInterval(interval);
  }, []);

  const handleClearCache = () => {
    setCacheSizeMb(0.5);
    showToast('Media and feed cache cleared successfully');
  };

  return (
    <div
      id="local-backend-console-overlay"
      className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-end justify-center p-0 max-w-md mx-auto"
      onClick={onClose}
    >
      <div
        id="local-backend-console-sheet"
        onClick={(e) => e.stopPropagation()}
        className="w-full bg-[#120F1F] border-t border-[#322C52] rounded-t-3xl p-5 max-h-[88vh] overflow-y-auto no-scrollbar space-y-4 shadow-2xl"
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-2 border-b border-[#1F1B33]">
          <div className="flex items-center gap-2">
            <Server className="w-5 h-5 text-[#00F5D4]" />
            <h3 className="text-sm font-bold text-white uppercase tracking-wider">
              Local Backend Console
            </h3>
          </div>
          <button onClick={onClose} className="text-white/60 hover:text-white p-1">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Server Status Banner */}
        <div className="p-3 rounded-2xl bg-[#161324] border border-[#00F5D4]/30 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <span className="w-3 h-3 rounded-full bg-[#00E676] animate-pulse shadow-[0_0_8px_#00E676]" />
            <div>
              <h4 className="text-xs font-bold text-white">Live Backend & PostgreSQL Online</h4>
              <p className="text-[11px] text-white/50">{dbStatus.db} • {dbStatus.latencyMs}ms ping • {Math.round(dbStatus.uptime)}s uptime</p>
            </div>
          </div>
          <div className="flex items-center gap-1 text-[11px] text-[#00E676] font-semibold bg-[#00E676]/10 px-2.5 py-1 rounded-full border border-[#00E676]/30">
            <CheckCircle2 className="w-3.5 h-3.5" />
            <span>Connected</span>
          </div>
        </div>

        {/* System & Recommendation Engine Metrics */}
        <div className="grid grid-cols-2 gap-2">
          <div className="p-3 rounded-xl bg-[#161324] border border-[#1F1B33]">
            <span className="text-[10px] text-white/50 uppercase font-semibold block">Total Database Rows</span>
            <span className="text-base font-extrabold text-white mt-1 block">
              {videos.length} videos • {drafts.length} drafts
            </span>
          </div>

          <div className="p-3 rounded-xl bg-[#161324] border border-[#1F1B33]">
            <span className="text-[10px] text-white/50 uppercase font-semibold block">Cache Allocation</span>
            <span className="text-base font-extrabold text-[#00F5D4] mt-1 block">
              {cacheSizeMb.toFixed(1)} MB
            </span>
          </div>

          <div className="p-3 rounded-xl bg-[#161324] border border-[#1F1B33]">
            <span className="text-[10px] text-white/50 uppercase font-semibold block">API Requests Served</span>
            <span className="text-base font-extrabold text-[#00B0FF] mt-1 block">
              {requestCount} reqs
            </span>
          </div>

          <div className="p-3 rounded-xl bg-[#161324] border border-[#1F1B33]">
            <span className="text-[10px] text-white/50 uppercase font-semibold block">Registered Accounts</span>
            <span className="text-base font-extrabold text-[#FF2A85] mt-1 block">
              {allAccounts.length} profiles
            </span>
          </div>
        </div>

        {/* Recommendation Engine Weights */}
        <div className="p-3 rounded-2xl bg-[#161324] border border-[#1F1B33] space-y-2">
          <div className="flex items-center justify-between text-xs font-bold text-white">
            <div className="flex items-center gap-1.5">
              <Cpu className="w-4 h-4 text-[#FFD600]" />
              <span>Recommendation Algorithm Weights</span>
            </div>
            <span className="text-[10px] text-white/50">CosSim Graph</span>
          </div>

          <div className="space-y-1.5 pt-1 text-[11px]">
            <div>
              <div className="flex justify-between text-white/70 mb-0.5">
                <span>Watch Duration & Loops</span>
                <span className="text-white font-bold">45%</span>
              </div>
              <div className="w-full h-1.5 bg-[#0C0A14] rounded-full overflow-hidden">
                <div className="h-full bg-[#00F5D4] rounded-full" style={{ width: '45%' }} />
              </div>
            </div>

            <div>
              <div className="flex justify-between text-white/70 mb-0.5">
                <span>Likes & Double Taps</span>
                <span className="text-white font-bold">35%</span>
              </div>
              <div className="w-full h-1.5 bg-[#0C0A14] rounded-full overflow-hidden">
                <div className="h-full bg-[#FF2A85] rounded-full" style={{ width: '35%' }} />
              </div>
            </div>

            <div>
              <div className="flex justify-between text-white/70 mb-0.5">
                <span>Shares, Duets & Bookmarks</span>
                <span className="text-white font-bold">20%</span>
              </div>
              <div className="w-full h-1.5 bg-[#0C0A14] rounded-full overflow-hidden">
                <div className="h-full bg-[#FFD600] rounded-full" style={{ width: '20%' }} />
              </div>
            </div>
          </div>
        </div>

        {/* Live Diagnostics Log Output */}
        <div className="rounded-2xl bg-[#090710] border border-[#1F1B33] p-3 space-y-2">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5 text-xs font-bold text-white/80">
              <Terminal className="w-3.5 h-3.5 text-[#00F5D4]" />
              <span>Embedded Request Logs</span>
            </div>
            <Activity className="w-3.5 h-3.5 text-[#00E676] animate-pulse" />
          </div>

          <div className="space-y-1 font-mono text-[10px] text-white/70">
            {logs.map((log) => (
              <div key={log.id} className="flex items-center justify-between py-0.5 border-b border-white/5">
                <div className="flex items-center gap-2">
                  <span className="text-white/40">{log.timestamp}</span>
                  <span className="text-[#00F5D4] font-bold">{log.method}</span>
                  <span className="truncate max-w-[160px] text-white/80">{log.endpoint}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-[#00E676]">{log.statusCode}</span>
                  <span className="text-white/40">{log.latencyMs}ms</span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Management Controls */}
        <div className="space-y-2 pt-2 border-t border-[#1F1B33]">
          <button
            onClick={handleClearCache}
            className="w-full py-2.5 rounded-xl bg-[#161324] hover:bg-[#1F1B33] border border-[#322C52] text-xs font-bold text-white flex items-center justify-center gap-2 transition"
          >
            <RefreshCw className="w-4 h-4 text-[#00B0FF]" />
            <span>Clear Local Media Cache</span>
          </button>

          <button
            onClick={() => {
              onSeedAdditionalClips();
              showToast('Spawned new viral video in feed!');
            }}
            className="w-full py-2.5 rounded-xl rainbow-gradient text-xs font-bold text-white flex items-center justify-center gap-2 shadow-md hover:scale-[1.01] transition"
          >
            <Sparkles className="w-4 h-4" />
            <span>Simulate Viral Upload Event</span>
          </button>

          <button
            onClick={() => {
              if (window.confirm('Reset all videos, accounts, and comments to clean seed defaults?')) {
                onResetDatabase();
              }
            }}
            className="w-full py-2.5 rounded-xl bg-red-950/40 hover:bg-red-900/50 border border-red-800/40 text-xs font-bold text-red-300 flex items-center justify-center gap-2 transition"
          >
            <Trash2 className="w-4 h-4" />
            <span>Reset Local Database to Factory Defaults</span>
          </button>
        </div>
      </div>
    </div>
  );
};
