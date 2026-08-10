/**
 * Banner for server-returned messages.
 *
 * Takes a list of messages because the backend returns per-field validation
 * detail, and showing only the first would leave the user fixing errors one at
 * a time.
 */
const TONES = {
  error: {
    wrapper: "bg-error-container border-error",
    text: "text-on-error-container",
    icon: "error",
  },
  success: {
    wrapper: "bg-secondary-container border-primary",
    text: "text-primary",
    icon: "check_circle",
  },
  info: {
    wrapper: "bg-secondary-container/30 border-secondary-container",
    text: "text-on-surface-variant",
    icon: "info",
  },
};

export default function Alert({ tone = "error", title, messages = [], children }) {
  const style = TONES[tone];

  return (
    <div
      role={tone === "error" ? "alert" : "status"}
      className={`flex items-start gap-3 rounded-r-lg border-l-4 p-4 shadow-sm ${style.wrapper}`}
    >
      <span className={`material-symbols-outlined filled mt-0.5 ${style.text}`}>
        {style.icon}
      </span>
      <div className="flex-1">
        {title && <h3 className={`text-label-md ${style.text}`}>{title}</h3>}
        {messages.length > 0 && (
          <ul className={`mt-1 list-inside list-disc space-y-1 text-body-md ${style.text}/90`}>
            {messages.map((message, index) => (
              <li key={index}>{message}</li>
            ))}
          </ul>
        )}
        {children}
      </div>
    </div>
  );
}