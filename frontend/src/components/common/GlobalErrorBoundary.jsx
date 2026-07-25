import React from 'react';
import { AlertOctagon, RefreshCw } from 'lucide-react';
import { Button } from '../ui/Button';

export class GlobalErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Uncaught React Error:', error, errorInfo);
  }

  handleReload = () => {
    this.setState({ hasError: false, error: null });
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-slate-950 text-slate-100 flex items-center justify-center p-6">
          <div className="max-w-md w-full glass-card p-8 rounded-2xl border border-slate-800 text-center space-y-6">
            <div className="w-16 h-16 rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-400 flex items-center justify-center mx-auto">
              <AlertOctagon className="w-8 h-8" />
            </div>
            <div className="space-y-2">
              <h1 className="text-xl font-extrabold text-white">Application Exception</h1>
              <p className="text-xs text-slate-400">
                An unexpected UI rendering error occurred. You can reload the page to restore your session.
              </p>
            </div>
            {this.state.error?.message && (
              <div className="bg-slate-900 p-3 rounded-xl text-left text-xs font-mono text-rose-300 border border-slate-800 overflow-x-auto">
                {this.state.error.message}
              </div>
            )}
            <Button onClick={this.handleReload} variant="primary" className="w-full">
              <RefreshCw className="w-4 h-4 mr-2" />
              Reload Application
            </Button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}
