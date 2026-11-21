# Implementation Notes

- Outstanding balance is `fee total - sum of existing payments`. A payment is rejected when it is greater than this balance.
- Multiple payments can settle a fee. The fee is marked fully paid when cumulative payments equal the fee total.
- Pending fees are queried through students with unpaid fee records; pagination applies to the student page, not the individual pending-fee list.
- School summaries use fee totals/counts from fee records and collected totals from payments. Outstanding amount is `total fees - collected amount`; record counts are calculated by paid and unpaid status.
