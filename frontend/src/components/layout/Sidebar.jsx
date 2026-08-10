import { NavLink } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

const NAV_ITEMS = [
  { to: "/dashboard", icon: "dashboard", label: "Dashboard" },
  { to: "/withdrawals/new", icon: "pending_actions", label: "New Withdrawal" },
  { to: "/withdrawals", icon: "assessment", label: "Withdrawal History" },
];

/**
 * Persistent navigation.
 *
 * NavLink rather than Link so react-router supplies the active state, instead
 * of comparing the current path by hand in each item.
 *
 * The Stitch design also showed Portfolios, Statements and Settings. Those are
 * omitted rather than stubbed — a nav item leading to an empty page reads worse
 * than a focused menu covering what the system actually does.
 */
export default function Sidebar() {
  const { investor, logout } = useAuth();

  return (
    <nav className="fixed left-0 top-0 z-50 flex h-screen w-64 flex-col border-r border-outline-variant bg-surface-container-lowest p-6">
      <div className="mb-8 flex items-center gap-3">
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary text-on-primary">
          <span className="material-symbols-outlined filled text-[20px]">eco</span>
        </div>
        <div>
          <h1 className="text-headline-sm text-primary">Enviro365</h1>
          <p className="text-label-sm text-on-surface-variant">Investments</p>
        </div>
      </div>

      <div className="flex-1 space-y-1">
        {NAV_ITEMS.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === "/withdrawals"}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-lg px-4 py-3 text-label-md transition-colors ${
                isActive
                  ? "bg-secondary-container font-bold text-primary"
                  : "text-on-surface-variant hover:bg-surface-container-high hover:text-primary"
              }`
            }
          >
            <span className="material-symbols-outlined text-[20px]">{item.icon}</span>
            <span>{item.label}</span>
          </NavLink>
        ))}
      </div>

      <div className="mt-auto space-y-4 border-t border-outline-variant pt-4">
        <div className="px-2">
          <p className="truncate text-label-md text-on-surface">{investor?.fullName}</p>
          <p className="truncate text-label-sm text-on-surface-variant">{investor?.email}</p>
        </div>
        <button
          onClick={logout}
          className="flex w-full items-center gap-3 rounded-lg px-4 py-3 text-label-md text-on-surface-variant transition-colors hover:bg-error-container hover:text-error"
        >
          <span className="material-symbols-outlined text-[20px]">logout</span>
          <span>Logout</span>
        </button>
      </div>
    </nav>
  );
}