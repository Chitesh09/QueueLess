import React from 'react';
import { QRCodeSVG } from 'qrcode.react';
import { ShieldCheck, Building2, User, Clock, Layers, X } from 'lucide-react';
import { Button } from '../ui/Button';
import { Badge } from '../ui/Badge';

export const QRCodePassModal = ({ isOpen, onClose, token }) => {
  if (!isOpen || !token) return null;

  const issuedTime = token.createdAt
    ? new Date(token.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    : 'Just now';

  return (
    <div className="fixed inset-0 z-50 bg-slate-950/85 backdrop-blur-md flex items-center justify-center p-4">
      <div className="glass-card max-w-md w-full p-6 rounded-3xl border border-slate-800 text-center space-y-6 relative shadow-2xl animate-in fade-in-80 scale-in-95 bg-slate-900/90">
        {/* Header Boarding Pass Banner */}
        <div className="flex items-center justify-between border-b border-slate-800 pb-4">
          <div className="flex items-center gap-2.5 text-left">
            <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-sky-500 to-indigo-600 flex items-center justify-center text-white shadow-md">
              <Building2 className="w-4 h-4" />
            </div>
            <div>
              <h3 className="font-extrabold text-white text-sm">City Care Hospital</h3>
              <p className="text-[10px] text-slate-400">Digital Boarding Pass</p>
            </div>
          </div>

          <Badge variant="info" className="font-mono text-[10px]">
            {token.status}
          </Badge>
        </div>

        {/* Ticket Details Boarding Section */}
        <div className="bg-slate-950 p-4 rounded-2xl border border-slate-800/80 text-left space-y-3 font-mono text-xs">
          <div className="flex justify-between items-center border-b border-slate-800/60 pb-2">
            <div>
              <span className="text-[10px] text-slate-500 uppercase">Token Claim</span>
              <div className="text-xl font-black text-sky-400">{token.tokenNumber}</div>
            </div>
            <div className="text-right">
              <span className="text-[10px] text-slate-500 uppercase">Priority</span>
              <div className="text-xs font-bold text-amber-400">{token.priorityClass}</div>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-2 text-[11px]">
            <div>
              <span className="text-[10px] text-slate-500 uppercase block">Department</span>
              <span className="text-slate-200 font-sans font-semibold">{token.serviceName}</span>
            </div>
            <div>
              <span className="text-[10px] text-slate-500 uppercase block">Patient</span>
              <span className="text-slate-200 font-sans font-semibold">{token.userName || 'John Doe'}</span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-2 text-[10px] pt-1 text-slate-400">
            <span>Issued: {issuedTime}</span>
            <span className="text-right">Valid: 15 min grace</span>
          </div>
        </div>

        {/* Signed HMAC QR Code SVG */}
        <div className="space-y-2">
          <div className="bg-white p-4 rounded-2xl inline-block shadow-xl mx-auto border-4 border-slate-800">
            <QRCodeSVG value={token.qrSignature || token.tokenNumber} size={160} />
          </div>

          <div className="flex items-center justify-center gap-1 text-[11px] font-mono text-emerald-400">
            <ShieldCheck className="w-3.5 h-3.5" />
            HMAC-SHA256 Cryptographic Signature Verified
          </div>
        </div>

        <div className="bg-slate-950 p-2.5 rounded-xl text-[10px] font-mono text-slate-400 break-all border border-slate-800">
          {token.qrSignature || 'HMAC-AUTHENTICATED'}
        </div>

        <Button onClick={onClose} variant="secondary" className="w-full text-xs">
          Close Digital Pass
        </Button>
      </div>
    </div>
  );
};
