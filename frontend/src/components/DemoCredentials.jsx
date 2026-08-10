import { useState } from "react";

/**
 * Assessment aid: surfaces the seeded accounts and the rule each one exercises.
 *
 * Included so a reviewer can exercise every business rule without reading
 * data.sql. This is scaffolding for the assessment, not a production feature —
 * a real deployment would remove it.
 */
const ACCOUNTS = [
  {
    name: "Thabo Mokoena",
    email: "thabo.mokoena@enviro365.co.za",
    age: 71,
    note: "Over 65 — retirement withdrawals permitted",
    cases: [
      "Green Savings R125 000 · withdraw R5 000 → succeeds",
      "Retirement Annuity R850 000 · withdraw R1 000 → succeeds (age passes)",
      "Withdraw R200 000 from savings → 422 exceeds balance",
      "Withdraw R120 000 from savings → 422 exceeds 90% ceiling",
    ],
  },
  {
    name: "Naledi Dlamini",
    email: "naledi.dlamini@enviro365.co.za",
    age: 33,
    note: "Under 65 — retirement withdrawals blocked",
    cases: [
      "Preservation Fund → card shows the age restriction, submit disabled",
      "Flexi Savings R47 500,50 · withdraw R1 000 → succeeds",
    ],
  },
  {
    name: "Sipho Khumalo",
    email: "sipho.khumalo@enviro365.co.za",
    age: 47,
    note: "Savings only — no withdrawal history on first load",
    cases: ["Money Market R89 200,75 · exercises the empty-history state"],
  },
];

const PASSWORD = "Password123!";

export default function DemoCredentials({ onUseAccount }) {
  const [open, setOpen] = useState(false);

  return (
    <div className="mt-6">
      <button
        type="button"
        onClick={() => setOpen((previous) => !previous)}
        aria-expanded={open}
        className="flex w-full items-center justify-center gap-2 rounded-lg border border-outline-variant bg-surface-container-low px-4 py-2 text-label-md text-on-surface-variant transition-colors hover:bg-surface-container hover:text-primary"
      >
        <span className="material-symbols-outlined text-[18px]">info</span>
        {open ? "Hide test accounts" : "Test accounts & scenarios"}
      </button>

      {open && (
        <div className="mt-4 space-y-4 rounded-xl border border-outline-variant bg-surface-container-lowest p-4">
          <p className="text-label-sm text-on-surface-variant">
            Seeded accounts. Password for all three:{" "}
            <code className="rounded bg-surface-container px-1.5 py-0.5 text-on-surface">
              {PASSWORD}
            </code>
          </p>

          {ACCOUNTS.map((account) => (
            <div
              key={account.email}
              className="rounded-lg border border-outline-variant p-3"
            >
              <div className="mb-2 flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <p className="text-label-md text-on-surface">
                    {account.name}{" "}
                    <span className="text-on-surface-variant">· age {account.age}</span>
                  </p>
                  <p className="truncate text-label-sm text-on-surface-variant">
                    {account.email}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => onUseAccount(account.email, PASSWORD)}
                  className="shrink-0 rounded-md bg-secondary-container px-2 py-1 text-label-sm text-on-secondary-container transition-colors hover:bg-primary hover:text-on-primary"
                >
                  Use
                </button>
              </div>

              <p className="mb-2 text-label-sm text-primary">{account.note}</p>

              <ul className="list-inside list-disc space-y-1 text-label-sm text-on-surface-variant">
                {account.cases.map((testCase, index) => (
                  <li key={index}>{testCase}</li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}