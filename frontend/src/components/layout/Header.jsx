import { useNavigate } from "react-router-dom";
import Button from "../common/Button";
import { useAuth } from "../../context/AuthContext";

export default function Header({ title }) {
  const navigate = useNavigate();
  const { investor } = useAuth();

  // Initials as an avatar — avoids shipping a placeholder photo of someone
  // who is not the actual user.
  const initials = (investor?.fullName ?? "")
    .split(" ")
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <header className="sticky top-0 z-40 flex h-20 w-full items-center justify-between border-b border-outline-variant bg-surface px-pad-desktop">
      <h2 className="text-headline-sm text-primary">{title}</h2>

      <div className="flex items-center gap-4">
        <Button
          variant="primary"
          icon="add"
          onClick={() => navigate("/withdrawals/new")}
          className="hidden py-2 sm:inline-flex"
        >
          New Notice
        </Button>
        <div className="flex h-10 w-10 items-center justify-center rounded-full bg-secondary-container text-label-md text-on-secondary-container">
          {initials || "—"}
        </div>
      </div>
    </header>
  );
}