import Spinner from "./Spinner";
import Alert from "./Alert";
import Button from "./Button";

/**
 * Renders the loading and error states shared by every data-backed screen.
 *
 * Centralised so a failed request always produces the same recovery affordance
 * rather than each page inventing its own.
 */
export default function StateView({ loading, error, onRetry, children }) {
  if (loading) {
    return (
      <div className="flex items-center justify-center py-24">
        <Spinner size={32} className="text-primary" />
      </div>
    );
  }

  if (error) {
    return (
      <Alert tone="error" title="Could not load this data" messages={[error.message]}>
        {onRetry && (
          <Button variant="outline" onClick={onRetry} className="mt-4 py-2">
            Try again
          </Button>
        )}
      </Alert>
    );
  }

  return children;
}