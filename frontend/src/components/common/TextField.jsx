/**
 * Labelled input with an optional leading icon and inline error.
 *
 * The error is rendered with role="alert" and linked via aria-describedby so
 * screen readers announce it, and aria-invalid marks the field itself.
 */
export default function TextField({
  id,
  label,
  icon,
  error,
  className = "",
  ...props
}) {
  const errorId = error ? `${id}-error` : undefined;

  return (
    <div className={className}>
      <label htmlFor={id} className="mb-2 block text-label-md text-on-surface">
        {label}
      </label>
      <div className="relative">
        {icon && (
          <span className="material-symbols-outlined pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant/60">
            {icon}
          </span>
        )}
        <input
          id={id}
          aria-invalid={Boolean(error)}
          aria-describedby={errorId}
          className={`block w-full rounded-lg border bg-surface-bright py-3 pr-3 text-body-md
            text-on-surface transition-colors focus:bg-surface-container-lowest focus:outline-none
            focus:ring-2 ${icon ? "pl-10" : "pl-4"}
            ${
              error
                ? "border-error focus:border-error focus:ring-error"
                : "border-outline-variant focus:border-primary focus:ring-primary"
            }`}
          {...props}
        />
      </div>
      {error && (
        <p id={errorId} role="alert" className="mt-2 flex items-center text-label-sm text-error">
          <span className="material-symbols-outlined mr-1 text-[14px]">error</span>
          {error}
        </p>
      )}
    </div>
  );
}