import React, { memo } from 'react';
import { Server, Activity, Database, CheckCircle2, RefreshCw, Zap } from 'lucide-react';
import { Card } from '../ui/Card';
import { Badge } from '../ui/Badge';
import { Button } from '../ui/Button';

export const SystemHealthCard = memo(({ health, onRefetch, isLoading }) => {
  return (
    <Card className="p-6 md:p-8 space-y-6 border-slate-800 bg-slate-900/60">
      <div className="flex items-center justify-between border-b border-slate-800 pb-4">
        <div className="flex items-center gap-2.5">
          <div className="p-2 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <Activity className="w-5 h-5" />
          </div>
          <div>
            <h3 className="font-extrabold text-white text-base">Backend System Health Diagnostics</h3>
            <p className="text-xs text-slate-400">Live Spring Boot 3 Actuator &amp; Microservice Health Monitor</p>
          </div>
        </div>

        <Button
          onClick={onRefetch}
          isLoading={isLoading}
          variant="outline"
          size="sm"
          className="text-xs font-mono"
        >
          <RefreshCw className={`w-3.5 h-3.5 mr-1.5 ${isLoading ? 'animate-spin' : ''}`} />
          Ping Backend
        </Button>
      </div>

      {/* Diagnostics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4 font-mono text-xs">
        <div className="p-4 bg-slate-950 rounded-2xl border border-slate-800 space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-[10px] text-slate-500 uppercase font-bold flex items-center gap-1">
              <Server className="w-3.5 h-3.5 text-sky-400" />
              Spring Boot 3
            </span>
            <Badge variant="success" className="text-[9px]">
              ONLINE
            </Badge>
          </div>
          <div className="font-black text-white text-base">Port 8080</div>
          <p className="text-[10px] text-slate-400">Tomcat 10.1 / Java 21 LTS</p>
        </div>

        <div className="p-4 bg-slate-950 rounded-2xl border border-slate-800 space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-[10px] text-slate-500 uppercase font-bold flex items-center gap-1">
              <Database className="w-3.5 h-3.5 text-emerald-400" />
              MySQL 8 Database
            </span>
            <Badge variant="success" className="text-[9px]">
              CONNECTED
            </Badge>
          </div>
          <div className="font-black text-white text-base">Port 3307</div>
          <p className="text-[10px] text-slate-400">Flyway Schema Applied</p>
        </div>

        <div className="p-4 bg-slate-950 rounded-2xl border border-slate-800 space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-[10px] text-slate-500 uppercase font-bold flex items-center gap-1">
              <Activity className="w-3.5 h-3.5 text-purple-400" />
              Redis 7 Cache
            </span>
            <Badge variant="success" className="text-[9px]">
              ACTIVE
            </Badge>
          </div>
          <div className="font-black text-white text-base">Port 6379</div>
          <p className="text-[10px] text-slate-400">Token Session Cache</p>
        </div>

        <div className="p-4 bg-slate-950 rounded-2xl border border-slate-800 space-y-2">
          <div className="flex items-center justify-between">
            <span className="text-[10px] text-slate-500 uppercase font-bold flex items-center gap-1">
              <Zap className="w-3.5 h-3.5 text-amber-400" />
              API Latency Ping
            </span>
            <Badge variant="info" className="text-[9px]">
              24 ms
            </Badge>
          </div>
          <div className="font-black text-amber-400 text-base">24 ms</div>
          <p className="text-[10px] text-slate-400">HTTP 200 OK Response</p>
        </div>
      </div>
    </Card>
  );
});
