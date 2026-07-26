# 🎓 QueueLess SaaS — Complete Masterclass Guide (From Scratch to Pro)

Welcome to the ultimate operational and technical masterclass for **QueueLess SaaS**! This guide covers everything from logging in as a first-time user to mastering multi-tenant enterprise operations, real-time STOMP WebSocket dispatching, and operations analytics.

---

## 📚 Table of Contents
1. [Module 1: Platform Overview & User Roles](#module-1-platform-overview--user-roles)
2. [Module 2: Customer Flow — Joining & Tracking Virtual Queues](#module-2-customer-flow--joining--tracking-virtual-queues)
3. [Module 3: Counter Operator Console & Keyboard Shortcuts](#module-3-counter-operator-console--keyboard-shortcuts)
4. [Module 4: Public Waiting Room TV Display Board](#module-4-public-waiting-room-tv-display-board)
5. [Module 5: Operations Analytics & BI Dashboard](#module-5-operations-analytics--bi-dashboard)
6. [Module 6: Organization & Branch Management](#module-6-organization--branch-management)
7. [Module 7: i18n, Accessibility & System Diagnostics](#module-7-i18n-accessibility--system-diagnostics)

---

## 👤 Module 1: Platform Overview & User Roles

QueueLess is designed for multi-tenant organizations (such as hospitals, banks, DMVs, and retail centers) to eliminate physical standing queues.

### The 4 Platform User Roles:
| Role | Primary Responsibilities | Access Level |
| :--- | :--- | :--- |
| **Customer** | Takes virtual tickets, selects priority level, tracks live wait-times on mobile | `/queue` |
| **Counter Operator** | Calls next customers, manages serving shifts, processes tickets | `/counter` |
| **Branch Admin** | Manages departments, counters, branch staff, and views analytics | `/organization`, `/analytics` |
| **Super Admin** | Platform-wide tenant management, billing, and system diagnostics | `/settings`, All Routes |

---

## 🎟️ Module 2: Customer Flow — Joining & Tracking Virtual Queues

### Step-by-Step Customer Journey:
1. **Access Queue Portal** (`/queue`):
   - Select your target **Branch** and **Department** (e.g. `City Hospital Central` → `Cardiology`).

2. **Select Service & Priority Triage**:
   - Choose a service (e.g., `General Checkup`, `Blood Test`, `Emergency Consultation`).
   - Select Priority Triage:
     - 🟢 **Standard**: Standard queue ordering.
     - 🟡 **Senior Citizen / PwD**: Prioritized ahead of standard tickets.
     - 🟣 **Appointment**: Pre-scheduled time slot prioritization.
     - 🔴 **Emergency**: Immediate top priority dispatch.

3. **Digital Ticket & Dynamic Timer**:
   - Upon joining, a digital ticket is generated (e.g., `CARD-104`).
   - The UI presents:
     - **Hero Token Number**
     - **Estimated Wait Time Countdown** (e.g. `12 min`)
     - **Customers Ahead Counter** (e.g. `3 ahead of you`)
     - **Assigned Counter Badge** (updates live when called)

4. **Digital HMAC QR Code Pass**:
   - Click **Show Digital QR Pass** to reveal a signed HMAC QR code pass for contactless kiosk check-in at the venue.

---

## 🖥️ Module 3: Counter Operator Console & Keyboard Shortcuts

The Operator Console (`/counter`) is built for maximum speed and ergonomic efficiency during busy shifts.

### Shift Operational Workflow:
1. **Set Shift Status**:
   - Toggle shift status to **ONLINE** (green indicator) to receive customers.
   - Toggle to **PAUSED** during lunch or breaks to temporarily halt dispatching.

2. **Call Next Customer (Weighted Round-Robin Algorithm)**:
   - Click **Call Next Customer** or press <kbd>N</kbd>.
   - The backend runs a **Weighted Round-Robin (WRR)** dispatch algorithm:
     - Evaluates ticket priority weights (Emergency = 10, Senior = 5, Appointment = 3, Standard = 1).
     - Dispatches the highest-priority waiting customer to your counter.
     - Triggers an instant STOMP WebSocket push notification to the customer's phone and the public TV display board!

3. **In-Service Timer & Ticket Processing**:
   - Once called, the status shifts to `IN_SERVICE` and an active serving stopwatch timer begins (<kbd>00:01</kbd>, <kbd>00:02</kbd>...).
   - **Complete Service**: Press <kbd>C</kbd> or click **Mark Completed** to finish service and log customer throughput metrics.
   - **No-Show / Skip**: Press <kbd>S</kbd> or click **Mark No-Show** if the customer fails to appear within 2 minutes.

### ⌨️ Global Keyboard Shortcuts Table (Power-User Pro Feature):
| Shortcut | Action | Description |
| :---: | :--- | :--- |
| <kbd>N</kbd> | **Call Next** | Calls the next highest priority customer in queue |
| <kbd>C</kbd> | **Complete** | Marks current ticket as completed and logs serving duration |
| <kbd>S</kbd> | **Skip / No-Show** | Marks customer as no-show and releases counter |
| <kbd>P</kbd> | **Toggle Pause** | Toggles operator shift between ONLINE and PAUSED |

---

## 📺 Module 4: Public Waiting Room TV Display Board

The TV Display Board (`/display-board`) is designed to run full-screen on wall-mounted TVs in waiting rooms.

### Features:
- **Hero Called Ticket Display**: Displays the currently called ticket in 120px glowing typography with counter assignment (e.g., `EMERG-001 → COUNTER 3`).
- **Audio Chime Notification**: Plays a crisp chime audio sound whenever a new customer is called.
- **Live Next-Up Ticker**: Displays a horizontal scrolling ticker of upcoming tokens waiting in line.
- **WebSocket STOMP Sync**: Updates in under 50 milliseconds without reloading the browser page.

---

## 📊 Module 5: Operations Analytics & BI Dashboard

The Analytics Dashboard (`/analytics`) gives executives and branch managers deep operational intelligence.

### Interactive Toolbar Controls:
- **Date Range Filter**: Select `TODAY`, `WEEK`, `MONTH`, or `CUSTOM`.
- **Hierarchical Dropdowns**: Filter by Organization, Branch, or Department.

### 6 Executive KPI Stat Cards:
1. **Total Served**: Cumulative customer volume processed.
2. **Average Wait Time**: Mean customer wait time in minutes.
3. **Average Service Time**: Mean customer handling time at counters.
4. **SLA Compliance %**: Percentage of customers served within target SLA limits.
5. **No-Show Rate**: Percentage of unfulfilled tokens.
6. **Active Counters**: Total counters currently ONLINE.

### 📈 Recharts Visual Analytics:
- **Peak Arrival Hours AreaChart**: Smooth gradient chart plotting hourly customer arrival volume to identify rush hours.
- **Department Throughput BarChart**: Comparative bar chart comparing customer volume handled across Cardiology, Orthopedics, Pediatrics, and Pharmacy.
- **SLA Compliance Donut PieChart**: Visual distribution showing tokens served within SLA vs. SLA breaches.

---

## 🏢 Module 6: Organization & Branch Management

The Organization Manager (`/organization`) allows administrators to configure physical service locations.

### Key Capabilities:
- **Create Organization**: Add new tenant entities with custom slug URLs and branding.
- **Create Branch**: Define physical branches (e.g., `City Hospital — Central Wing`).
- **Counter Allocation**: Add, rename, or reassign physical counters to specific departments.

---

## ⚙️ Module 7: i18n, Accessibility & System Diagnostics

### 🌐 Multi-Language Switching:
Click the language selector in the top-right header to switch between:
- 🇺🇸 **English** (`en`)
- 🇪🇸 **Spanish** (`es`)
- 🇫🇷 **French** (`fr`)
- 🇮🇳 **Hindi** (`hi`)

### ♿ Accessibility Features (WCAG 2.1 AA):
- **High Contrast Theme**: Toggle high contrast mode for enhanced visibility.
- **Skip to Main Content**: Press <kbd>Tab</kbd> on load to bypass navigation directly to main content (`#main-content`).
- **Screen Reader Announcers**: Live ARIA region (`aria-live="polite"`) announcing real-time queue updates to screen readers.

### 🩺 System Diagnostics (`/settings`):
- Displays real-time Spring Boot Actuator status, MySQL 8 port status, Redis 7 cache health, and a live API round-trip ping timer (e.g. `24 ms`).
