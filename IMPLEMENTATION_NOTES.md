# Implementation Notes

- **Projections:** Used projection interfaces for fee-detail and school aggregate queries to return only the required fee, payment, and count fields.

- **Payment Processing:** Payment recording and fee status updates are handled in a single transaction; the payment is saved before recalculating and persisting the fee record's `fullyPaid` state.

- **Partial Payments:** A fee record accepts multiple payments. The service compares cumulative paid amount with the fee total to determine whether the fee is fully paid and rejects payments that exceed the remaining outstanding balance.

- **Pagination:** Pending-student queries use student-level pagination, with pending fee details resolved for each page.

- **School Fee Summary:** Fee totals/counts and payment totals are queried separately, avoiding incorrect totals that could result from joining fee and payment records.

- **API Response Format:** API responses consistently use `statusCode`, `message`, and `data`.

- **Lombok:** Used Lombok annotations such as @Getter, @Setter, and @RequiredArgsConstructor to reduce boilerplate code and avoid manually writing getters, setters, and constructors.
