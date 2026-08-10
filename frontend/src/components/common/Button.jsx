import Spinner from "./Spinner";

/**
 * Shared button.
 *
 * Variants map to the design's colour roles rather than accepting arbitrary
 * classes, so every button in the app stays on-palette.
 */
const VARIANTS = {
  primary:
    "bg-primary text-on-primary hover:bg-tertiary shadow-sm disabled:bg-surface-variant disabled:text-on-surface-variant",
  outline:
    "bg-transparent border border-outline-variant text-on-surface hover:bg-surface-container",
  ghost: "bg-transparent text-on-surface-variant hover:text-primary",
};

export default function Button({
  variant = "primary",
  loading = false,
  disabled = false,
  icon,
  children,
  className = "",
  ...props
}) {
  return (
    <button
      disabled={disabled || loading}
      className={`inline-flex items-center justify-center gap-2 rounded-lg px-4 py-3 text-label-md
        transition-colors focus:outline-none focus:ring-2 focus:ring-primary focus:ring-offset-2
        disabled:cursor-not-allowed ${VARIANTS[variant]} ${className}`}
      {...props}
    >
      {loading ? (
        <Spinner size={16} />
      ) : (
        icon && <span className="material-symbols-outlined text-[18px]">{icon}</span>
      )}
      {children}
    </button>
  );
}