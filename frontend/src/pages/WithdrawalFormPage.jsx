import { useCallback, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import AppShell from "../components/layout/AppShell";
import StateView from "../components/common/StateView";
import Button from "../components/common/Button";
import Alert from "../components/common/Alert";
import Spinner from "../components/common/Spinner";
import { useApi } from "../hooks/useApi";
import { fetchPortfolio, fetchEligibility } from "../api/portfolio";
import { createWithdrawal } from "../api/withdrawals";
import { formatZAR } from "../utils/format";

export default function WithdrawalFormPage() {
  const navigate = useNavigate();

  const loadPortfolio = useCallback(() => fetchPortfolio(), []);
  const { data: portfolio, loading, error, refetch } = useApi(loadPortfolio);

  const [selectedId, setSelectedId] = useState(null);
  const [eligibility, setEligibility] = useState(null);
  const [checkingEligibility, setCheckingEligibility] = useState(false);
  const [amount, setAmount] = useState("");
  const [amountError, setAmountError] = useState(null);
  const [serverError, setServerError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState([]);
  const [success, setSuccess] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const products = portfolio?.products ?? [];
  const selected = products.find((product) => product.id === selectedId) ?? null;

  /**
   * Fetches eligibility whenever the selected product changes.
   *
   * The rules live on the server, so the UI asks rather than reimplementing
   * them. Duplicating the age and percentage logic in JavaScript would mean two
   * sources of truth that can drift apart.
   */
  useEffect(() => {
    if (!selectedId) {
      setEligibility(null);
      return;
    }

    let active = true;
    setCheckingEligibility(true);
    setEligibility(null);

    fetchEligibility(selectedId)
      .then((result) => active && setEligibility(result))
      .catch(() => active && setEligibility(null))
      .finally(() => active && setCheckingEligibility(false));

    return () => {
      active = false;
    };
  }, [selectedId]);

  /**
   * Client-side amount validation.
   *
   * Mirrors the server rules purely to give immediate feedback — the backend
   * still enforces every one of them, and a request that slips past this is
   * rejected there.
   */
  function validateAmount(value) {
    const numeric = Number(value);

    if (!value.trim()) return "Enter an amount";
    if (Number.isNaN(numeric)) return "Enter a valid number";
    if (numeric <= 0) return "Amount must be greater than zero";
    if (!/^\d+(\.\d{1,2})?$/.test(value.trim())) {
      return "Amount may have at most 2 decimal places";
    }
    if (selected && numeric > Number(selected.currentBalance)) {
      return `Exceeds the available balance of ${formatZAR(selected.currentBalance)}`;
    }
    if (eligibility && numeric > Number(eligibility.maximumWithdrawal)) {
      return `Exceeds the maximum of ${formatZAR(eligibility.maximumWithdrawal)} (90% of balance)`;
    }
    return null;
  }

  function handleAmountChange(event) {
    const value = event.target.value;
    setAmount(value);
    setAmountError(validateAmount(value));
    setServerError(null);
    setFieldErrors([]);
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setServerError(null);
    setFieldErrors([]);
    setSuccess(null);

    const validationError = validateAmount(amount);
    if (validationError) {
      setAmountError(validationError);
      return;
    }

    setSubmitting(true);
    try {
      const result = await createWithdrawal(selectedId, Number(amount));
      setSuccess(result);
      setAmount("");
      // Refetches the portfolio so the balance shown reflects the debit.
      await refetch();
      const updated = await fetchEligibility(selectedId);
      setEligibility(updated);
    } catch (err) {
      // 400 carries per-field detail; 422 carries a single business message.
      // Surfacing them differently means the user sees the specific reason.
      if (err.status === 400 && err.fieldErrors?.length) {
        setFieldErrors(err.fieldErrors.map((fe) => fe.message));
      } else {
        setServerError(err.message);
      }
    } finally {
      setSubmitting(false);
    }
  }

  const canSubmit =
    selectedId &&
    eligibility?.eligible &&
    amount.trim() &&
    !amountError &&
    !submitting;

  return (
    <AppShell title="New Withdrawal">
      <StateView loading={loading} error={error} onRetry={refetch}>
        <div className="mb-8">
          <h2 className="text-headline-lg text-on-surface">New Withdrawal Request</h2>
          <p className="mt-2 text-body-md text-on-surface-variant">
            Submit a notice to withdraw funds from one of your active products.
          </p>
        </div>

        {success && (
          <div className="mb-6">
            <Alert tone="success" title={`Withdrawal ${success.reference} submitted`}>
              <p className="mt-1 text-body-md text-primary">
                {formatZAR(success.amount)} withdrawn from {success.productName}. New
                balance: {formatZAR(success.balanceAfter)}.
              </p>
              <Button
                variant="outline"
                onClick={() => navigate("/withdrawals")}
                className="mt-4 py-2"
              >
                View history
              </Button>
            </Alert>
          </div>
        )}

        {serverError && (
          <div className="mb-6">
            <Alert tone="error" title="Withdrawal declined" messages={[serverError]} />
          </div>
        )}

        {fieldErrors.length > 0 && (
          <div className="mb-6">
            <Alert tone="error" title="Please correct the following" messages={fieldErrors} />
          </div>
        )}

        <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-gutter lg:grid-cols-12">
          <div className="space-y-6 lg:col-span-8">
            {/* Step 1 — product selection */}
            <section className="rounded-xl border border-surface-variant bg-surface-container-lowest p-margin-lg shadow-[0_4px_6px_-1px_rgba(0,0,0,0.05)]">
              <h3 className="mb-6 flex items-center border-b border-surface-variant pb-4 text-headline-sm text-on-surface">
                <span className="mr-3 flex h-8 w-8 items-center justify-center rounded-full bg-surface-container text-label-md text-primary">
                  1
                </span>
                Select Product
              </h3>

              <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                {products.map((product) => {
                  const isSelected = product.id === selectedId;
                  const isIneligible =
                    isSelected && eligibility && !eligibility.eligible;

                  return (
                    <button
                      key={product.id}
                      type="button"
                      onClick={() => {
                        setSelectedId(product.id);
                        setAmount("");
                        setAmountError(null);
                        setSuccess(null);
                        setServerError(null);
                      }}
                      className={`rounded-lg border p-4 text-left transition-all ${
                        isIneligible
                          ? "border-2 border-error bg-error-container/10"
                          : isSelected
                            ? "border-2 border-primary bg-secondary-container/20"
                            : "border-outline-variant bg-surface hover:border-primary/50"
                      }`}
                    >
                      <div className="mb-3 flex items-center gap-3">
                        <div
                          className={`flex h-10 w-10 items-center justify-center rounded-full ${
                            product.productType === "RETIREMENT"
                              ? "bg-tertiary-container text-on-tertiary-container"
                              : "bg-secondary-container text-on-secondary-container"
                          }`}
                        >
                          <span className="material-symbols-outlined text-[20px]">
                            {product.productType === "RETIREMENT"
                              ? "account_balance"
                              : "savings"}
                          </span>
                        </div>
                        <div>
                          <p className="text-label-sm uppercase tracking-wider text-on-surface-variant">
                            {product.productType}
                          </p>
                          <p className="text-label-md text-on-surface">
                            {product.productName}
                          </p>
                        </div>
                      </div>

                      <div className="mt-4 border-t border-outline-variant pt-4">
                        <p className="mb-1 text-label-sm text-on-surface-variant">
                          Available Balance
                        </p>
                        <p className="text-headline-sm text-on-surface">
                          {formatZAR(product.currentBalance)}
                        </p>
                      </div>

                      {isSelected && checkingEligibility && (
                        <div className="mt-3 flex items-center gap-2 text-label-sm text-on-surface-variant">
                          <Spinner size={14} />
                          Checking eligibility…
                        </div>
                      )}

                      {isIneligible && (
                        <div className="mt-3 flex items-start gap-2 rounded bg-error/10 p-2">
                          <span className="material-symbols-outlined mt-0.5 text-[16px] text-error">
                            info
                          </span>
                          <p className="text-label-sm leading-tight text-error">
                            {eligibility.reason}
                          </p>
                        </div>
                      )}
                    </button>
                  );
                })}
              </div>
            </section>

            {/* Step 2 — amount */}
            <section className="rounded-xl border border-surface-variant bg-surface-container-lowest p-margin-lg shadow-[0_4px_6px_-1px_rgba(0,0,0,0.05)]">
              <h3 className="mb-6 flex items-center border-b border-surface-variant pb-4 text-headline-sm text-on-surface">
                <span className="mr-3 flex h-8 w-8 items-center justify-center rounded-full bg-surface-container text-label-md text-primary">
                  2
                </span>
                Withdrawal Amount
              </h3>

              <label htmlFor="amount" className="mb-2 block text-label-md text-on-surface">
                Amount (ZAR)
              </label>
              <div className="relative">
                <span className="absolute left-4 top-1/2 -translate-y-1/2 text-headline-md text-on-surface-variant">
                  R
                </span>
                <input
                  id="amount"
                  type="text"
                  inputMode="decimal"
                  placeholder="0.00"
                  value={amount}
                  onChange={handleAmountChange}
                  disabled={!selectedId || !eligibility?.eligible}
                  aria-invalid={Boolean(amountError)}
                  aria-describedby={amountError ? "amount-error" : undefined}
                  className={`w-full rounded-lg border-2 bg-surface-bright py-3 pl-10 pr-4 text-headline-md
                    text-on-surface transition-colors focus:outline-none disabled:cursor-not-allowed
                    disabled:opacity-50 ${
                      amountError
                        ? "border-error bg-error-container/10"
                        : "border-outline-variant focus:border-primary"
                    }`}
                />
              </div>

              {amountError && (
                <p id="amount-error" role="alert" className="mt-2 flex items-center text-label-sm text-error">
                  <span className="material-symbols-outlined mr-1 text-[14px]">close</span>
                  {amountError}
                </p>
              )}

              {eligibility?.eligible && !amountError && (
                <p className="mt-2 text-label-sm text-on-surface-variant">
                  Maximum permitted: {formatZAR(eligibility.maximumWithdrawal)}
                </p>
              )}
            </section>
          </div>

          {/* Summary */}
          <div className="lg:col-span-4">
            <div className="sticky top-28 rounded-xl border border-surface-variant bg-surface-container-lowest p-margin-lg shadow-[0_4px_6px_-1px_rgba(0,0,0,0.05)]">
              <h3 className="mb-4 text-headline-sm text-on-surface">Request Summary</h3>

              <dl className="mb-6 space-y-4">
                <div className="flex justify-between border-b border-surface-variant py-2">
                  <dt className="text-body-md text-on-surface-variant">Product</dt>
                  <dd className="text-right text-label-md text-on-surface">
                    {selected?.productName ?? "—"}
                  </dd>
                </div>
                <div className="flex justify-between border-b border-surface-variant py-2">
                  <dt className="text-body-md text-on-surface-variant">Available</dt>
                  <dd className="text-label-md text-on-surface">
                    {selected ? formatZAR(selected.currentBalance) : "—"}
                  </dd>
                </div>
                <div className="flex justify-between rounded bg-surface-container p-3">
                  <dt className="text-label-md text-on-surface">Max Allowed (90%)</dt>
                  <dd className="text-label-md text-primary">
                    {eligibility?.eligible
                      ? formatZAR(eligibility.maximumWithdrawal)
                      : "—"}
                  </dd>
                </div>
                <div className="flex justify-between py-2">
                  <dt className="text-body-md text-on-surface-variant">Requested</dt>
                  <dd
                    className={`text-label-md ${amountError ? "text-error line-through" : "text-on-surface"}`}
                  >
                    {amount ? formatZAR(amount) : "—"}
                  </dd>
                </div>
              </dl>

              <Button
                type="submit"
                loading={submitting}
                disabled={!canSubmit}
                icon={canSubmit ? undefined : "lock"}
                className="w-full"
              >
                {submitting ? "Submitting…" : "Submit Request"}
              </Button>

              <Button
                type="button"
                variant="ghost"
                onClick={() => navigate("/dashboard")}
                className="mt-2 w-full"
              >
                Cancel
              </Button>
            </div>
          </div>
        </form>
      </StateView>
    </AppShell>
  );
}