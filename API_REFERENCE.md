# FirstClub Membership Program — API Reference

All APIs return JSON with the envelope:
```json
{ "success": true, "message": "...", "data": { ... }, "timestamp": "..." }
```

**Base URL:** `http://localhost:8080`  
**Swagger UI:** `http://localhost:8080/swagger-ui.html`  
**H2 Console:** `http://localhost:8080/h2-console` (JDBC: `jdbc:h2:mem:membershipdb`, User: `sa`)

---

## 1. Membership Plans & Tiers

### Get All Plans (with Tiers)
```bash
curl -s http://localhost:8080/api/v1/plans | jq .
```

### Get a Specific Plan
```bash
curl -s http://localhost:8080/api/v1/plans/1 | jq .
```

### Get All Tiers (Silver / Gold / Platinum)
```bash
curl -s http://localhost:8080/api/v1/tiers | jq .
```

---

## 2. Subscriptions

### Subscribe to Monthly + Silver
```bash
curl -s -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 101,
    "planId": 1,
    "tierLevel": "SILVER"
  }' | jq .
```

### Subscribe to Yearly + Gold
```bash
curl -s -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 102,
    "planId": 3,
    "tierLevel": "GOLD"
  }' | jq .
```

### Get Active Membership for a User
```bash
curl -s http://localhost:8080/api/v1/subscriptions/user/101 | jq .
```

### Upgrade Tier (Silver → Gold)
> First, get the membershipId from the subscribe response.
```bash
curl -s -X PUT http://localhost:8080/api/v1/subscriptions/1/upgrade \
  -H "Content-Type: application/json" \
  -d '{
    "targetTierLevel": "GOLD",
    "reason": "Eligible based on order activity"
  }' | jq .
```

### Upgrade Tier (Gold → Platinum)
```bash
curl -s -X PUT http://localhost:8080/api/v1/subscriptions/1/upgrade \
  -H "Content-Type: application/json" \
  -d '{
    "targetTierLevel": "PLATINUM",
    "reason": "Heavy usage this month"
  }' | jq .
```

### Downgrade Tier (Platinum → Gold)
```bash
curl -s -X PUT http://localhost:8080/api/v1/subscriptions/1/downgrade \
  -H "Content-Type: application/json" \
  -d '{
    "targetTierLevel": "GOLD",
    "reason": "User requested downgrade"
  }' | jq .
```

### Cancel Membership
```bash
curl -s -X DELETE http://localhost:8080/api/v1/subscriptions/1 | jq .
```

### Get Membership History (Audit Log)
```bash
curl -s http://localhost:8080/api/v1/subscriptions/user/101/history | jq .
```

---

## 3. Benefits & Checkout

### Get Active Benefits for a User
```bash
curl -s http://localhost:8080/api/v1/benefits/user/101 | jq .
```

### Compute Checkout Benefits (Cart = ₹1,500, delivery eligible)
```bash
curl -s -X POST http://localhost:8080/api/v1/benefits/checkout \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 101,
    "cartTotal": 1500.00,
    "deliveryEligible": true
  }' | jq .
```

**Example response for Gold member (10% discount + free delivery):**
```json
{
  "success": true,
  "data": {
    "userId": 101,
    "tierLevel": "GOLD",
    "originalTotal": 1500.00,
    "discountPercentage": 10.00,
    "discountAmount": 150.00,
    "finalTotal": 1350.00,
    "freeDeliveryApplied": true,
    "deliverySaving": 49.00,
    "hasMembership": true,
    "appliedBenefits": ["10% discount on cart", "Free delivery", "Exclusive deals access", "Early sale access", "2 exclusive coupons/month"]
  }
}
```

---

## 4. Admin / Order Events

### Record an Order Event (triggers tier evaluation)
```bash
curl -s -X POST http://localhost:8080/api/v1/admin/orders/event \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 101,
    "orderId": "ORD-20240001",
    "orderValue": 500.00
  }' | jq .
```

### Record Multiple Orders to Trigger Gold Promotion
> Gold requires: 7+ orders AND ₹3,000+ value in 30 days
```bash
for i in $(seq 1 7); do
  curl -s -X POST http://localhost:8080/api/v1/admin/orders/event \
    -H "Content-Type: application/json" \
    -d "{\"userId\": 101, \"orderId\": \"ORD-GOLD-$i\", \"orderValue\": 500.00}" | jq .data.orderCountThisMonth
done
```

### Record Orders for COHORT-based tier test
```bash
curl -s -X POST http://localhost:8080/api/v1/admin/orders/event \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 103,
    "orderId": "ORD-VIP-001",
    "orderValue": 200.00,
    "cohortKey": "VIP_2025"
  }' | jq .
```

### Manually Trigger Tier Evaluation
```bash
curl -s -X POST http://localhost:8080/api/v1/admin/tiers/evaluate/101 | jq .
```

### Get User Order Stats
```bash
curl -s http://localhost:8080/api/v1/admin/stats/101 | jq .
```

---

## 5. End-to-End Demo Flow

Run these in order to demonstrate the complete happy path:

```bash
# Step 1: Browse plans
curl -s http://localhost:8080/api/v1/plans | jq '.data[].name'

# Step 2: Subscribe user 101 to Monthly / Silver
curl -s -X POST http://localhost:8080/api/v1/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"userId": 101, "planId": 1, "tierLevel": "SILVER"}' | jq .

# Step 3: Check membership + benefits
curl -s http://localhost:8080/api/v1/subscriptions/user/101 | jq '{tier: .data.tierLevel, benefits: [.data.activeBenefits[].benefitType]}'

# Step 4: Simulate checkout (₹2,000 cart)
curl -s -X POST http://localhost:8080/api/v1/benefits/checkout \
  -H "Content-Type: application/json" \
  -d '{"userId": 101, "cartTotal": 2000.00, "deliveryEligible": true}' | jq '{discount: .data.discountAmount, freeDelivery: .data.freeDeliveryApplied, finalTotal: .data.finalTotal}'

# Step 5: Simulate 7 orders (triggers auto Gold promotion)
for i in $(seq 1 7); do
  curl -s -X POST http://localhost:8080/api/v1/admin/orders/event \
    -H "Content-Type: application/json" \
    -d "{\"userId\": 101, \"orderId\": \"ORD-$i\", \"orderValue\": 500.00}" > /dev/null
done

# Step 6: Check tier was auto-promoted
curl -s http://localhost:8080/api/v1/subscriptions/user/101 | jq '.data.tierLevel'

# Step 7: View history
curl -s http://localhost:8080/api/v1/subscriptions/user/101/history | jq '[.data[] | {change: .changeType, from: .previousTier, to: .newTier}]'

# Step 8: Manual upgrade to Platinum
curl -s -X PUT http://localhost:8080/api/v1/subscriptions/1/upgrade \
  -H "Content-Type: application/json" \
  -d '{"targetTierLevel": "PLATINUM", "reason": "VIP customer"}' | jq '.data.tierLevel'

# Step 9: Cancel
curl -s -X DELETE http://localhost:8080/api/v1/subscriptions/1 | jq .message
```

---

## Error Responses

| HTTP Code | Scenario |
|---|---|
| 400 | Validation error or invalid operation (e.g., upgrading to a lower tier) |
| 404 | Plan, tier, or membership not found |
| 409 | User already has active membership (duplicate subscribe) |
| 409 | Concurrent modification conflict (optimistic lock) — retry request |
| 500 | Unexpected server error |

---

## Postman Import

1. Open Postman → Import → Raw text
2. Paste any `curl` command — Postman will auto-parse it
3. Or use **Swagger UI** at `http://localhost:8080/swagger-ui.html` to test interactively (try it!)
