import Sidebar from "./Sidebar";
import Header from "./Header";

/**
 * Wraps every authenticated page so the sidebar and header are declared once
 * rather than repeated on each screen.
 */
export default function AppShell({ title, children }) {
  return (
    <div className="flex min-h-full bg-surface">
      <Sidebar />
      <div className="ml-64 flex min-h-screen min-w-0 flex-1 flex-col">
        <Header title={title} />
        <main className="flex-1 overflow-y-auto p-gutter">
          <div className="mx-auto max-w-[1440px] space-y-gutter pb-12">{children}</div>
        </main>
      </div>
    </div>
  );
}