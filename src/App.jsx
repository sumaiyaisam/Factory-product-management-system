import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Activity, Archive, ArrowDownToLine, ArrowUpFromLine, Boxes, Building2,
  Check, ChevronDown, CircleHelp, ClipboardList, LayoutDashboard, MapPin,
  Menu, Package, Plus, RefreshCw, Search, Settings2, Shapes, Truck, X,
} from "lucide-react";
import api, { getErrorMessage } from "./api";
import "./App.css";

const NAV = [
  { id: "dashboard", label: "Dashboard", icon: LayoutDashboard },
  { id: "products", label: "Products", icon: Package },
  { id: "categories", label: "Categories", icon: Shapes },
  { id: "uom", label: "Units of Measure", icon: Boxes },
  { id: "warehouses", label: "Warehouses", icon: Building2 },
  { id: "locations", label: "Storage Locations", icon: MapPin },
  { id: "inventory", label: "Inventory", icon: ClipboardList },
];

const CONFIG = {
  products: {
    title: "Products", singular: "Product", endpoint: "/products",
    description: "Manage your product master and pricing information.",
    fields: [
      { name: "sku", label: "SKU / Product Code", required: true },
      { name: "name", label: "Product Name", required: true },
      { name: "description", label: "Description", type: "textarea" },
      { name: "categoryId", label: "Category", type: "select", source: "categories", required: true },
      { name: "uomId", label: "Unit of Measure", type: "select", source: "uom", required: true },
      { name: "sellingPrice", label: "Selling Price (LKR)", type: "number", required: true },
      { name: "costPrice", label: "Cost Price (LKR)", type: "number", required: true },
      { name: "active", label: "Active", type: "checkbox", defaultValue: true },
    ],
    columns: [
      ["sku", "SKU"], ["name", "Product"], ["categoryName", "Category"],
      ["uomName", "Unit"], ["sellingPrice", "Selling Price"], ["costPrice", "Cost Price"], ["active", "Status"],
    ],
  },
  categories: {
    title: "Categories", singular: "Category", endpoint: "/categories",
    description: "Group products into meaningful business categories.",
    fields: [
      { name: "code", label: "Category Code", required: true },
      { name: "name", label: "Category Name", required: true },
      { name: "description", label: "Description", type: "textarea" },
      { name: "active", label: "Active", type: "checkbox", defaultValue: true },
    ],
    columns: [["code", "Code"], ["name", "Category"], ["description", "Description"], ["active", "Status"]],
  },
  uom: {
    title: "Units of Measure", singular: "Unit", endpoint: "/uom",
    description: "Maintain units used to measure product quantities.",
    fields: [
      { name: "code", label: "Unit Code", required: true },
      { name: "name", label: "Unit Name", required: true },
      { name: "description", label: "Description", type: "textarea" },
      { name: "active", label: "Active", type: "checkbox", defaultValue: true },
    ],
    columns: [["code", "Code"], ["name", "Unit"], ["description", "Description"], ["active", "Status"]],
  },
  warehouses: {
    title: "Warehouses", singular: "Warehouse", endpoint: "/warehouses",
    description: "Manage physical warehouse facilities.",
    fields: [
      { name: "code", label: "Warehouse Code", required: true },
      { name: "name", label: "Warehouse Name", required: true },
      { name: "description", label: "Description", type: "textarea" },
      { name: "address", label: "Address" },
      { name: "city", label: "City" },
      { name: "warehouseType", label: "Warehouse Type", type: "selectStatic", options: [
        "GENERAL", "RAW_MATERIAL", "FINISHED_GOODS", "PACKAGING_MATERIAL", "CHEMICAL", "DISTRIBUTION",
      ], required: true },
      { name: "active", label: "Active", type: "checkbox", defaultValue: true },
    ],
    columns: [["code", "Code"], ["name", "Warehouse"], ["city", "City"], ["warehouseType", "Type"], ["active", "Status"]],
  },
  locations: {
    title: "Storage Locations", singular: "Storage Location", endpoint: "/locations",
    description: "Organize and identify locations inside each warehouse.",
    fields: [
      { name: "warehouseId", label: "Warehouse", type: "select", source: "warehouses", required: true },
      { name: "code", label: "Location Code", required: true },
      { name: "name", label: "Location Name", required: true },
      { name: "description", label: "Description", type: "textarea" },
      { name: "locationType", label: "Location Type", type: "selectStatic", options: [
        "GENERAL", "RAW_MATERIAL", "FINISHED_GOODS", "PACKAGING", "CHEMICAL_STORAGE",
        "QUALITY_CONTROL", "DISPATCH", "RECEIVING",
      ], required: true },
      { name: "active", label: "Active", type: "checkbox", defaultValue: true },
    ],
    columns: [["code", "Code"], ["name", "Location"], ["warehouseName", "Warehouse"], ["locationType", "Type"], ["active", "Status"]],
  },
};

const emptyForm = (config) => Object.fromEntries((config?.fields || []).map((f) => [f.name, f.defaultValue ?? (f.type === "checkbox" ? true : "")]));
const labelize = (value) => String(value ?? "").replaceAll("_", " ").replace(/([a-z])([A-Z])/g, "$1 $2").replace(/\b\w/g, (m) => m.toUpperCase());
const money = (value) => `LKR ${Number(value || 0).toLocaleString("en-LK", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;

function App() {
  const [page, setPage] = useState("dashboard");
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [data, setData] = useState({ products: [], categories: [], uom: [], warehouses: [], locations: [], balances: [], transactions: [] });
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState("");
  const [notice, setNotice] = useState(null);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({});
  const [saving, setSaving] = useState(false);

  const notify = (type, message) => {
    setNotice({ type, message });
    window.setTimeout(() => setNotice(null), 4200);
  };

  const loadData = useCallback(async () => {
    setLoading(true);
    const tasks = [
      ["products", () => api.get("/products")],
      ["categories", () => api.get("/categories")],
      ["uom", () => api.get("/uom")],
      ["warehouses", () => api.get("/warehouses")],
      ["locations", () => api.get("/locations")],
      ["balances", () => api.get("/inventory/balances")],
      ["transactions", () => api.get("/inventory/transactions")],
    ];
    const result = {};
    const failures = [];
    await Promise.all(tasks.map(async ([key, fn]) => {
      try {
        const response = await fn();
        result[key] = Array.isArray(response.data) ? response.data : response.data?.content || [];
      } catch (error) {
        result[key] = [];
        failures.push(`${key}: ${getErrorMessage(error)}`);
      }
    }));
    setData((old) => ({ ...old, ...result }));
    setLoading(false);
    if (failures.length) notify("error", `Some data could not load. Check backend/CORS. ${failures[0]}`);
  }, []);

  useEffect(() => { loadData(); }, [loadData]);

  const currentConfig = CONFIG[page];
  const openCreate = () => {
    setForm(emptyForm(currentConfig));
    setModal({ kind: "create", page });
  };
  const openEdit = (item) => {
    const next = { ...item };
    if (page === "products") {
      next.categoryId = item.categoryId ?? item.category?.id ?? "";
      next.uomId = item.uomId ?? item.unitOfMeasure?.id ?? "";
    }
    if (page === "locations") next.warehouseId = item.warehouseId ?? item.warehouse?.id ?? "";
    setForm({ ...emptyForm(currentConfig), ...next });
    setModal({ kind: "edit", page, item });
  };

  const saveMaster = async (event) => {
    event.preventDefault();
    const config = CONFIG[modal.page];
    const payload = { ...form };
    config.fields.forEach((field) => {
      if (field.type === "number" && payload[field.name] !== "") payload[field.name] = Number(payload[field.name]);
      if (field.type === "select" && payload[field.name] !== "") payload[field.name] = Number(payload[field.name]);
      if (field.type === "checkbox") payload[field.name] = Boolean(payload[field.name]);
      if (payload[field.name] === "") delete payload[field.name];
    });
    setSaving(true);
    try {
      if (modal.kind === "edit") await api.put(`${config.endpoint}/${modal.item.id}`, payload);
      else await api.post(config.endpoint, payload);
      notify("success", `${config.singular} ${modal.kind === "edit" ? "updated" : "created"} successfully.`);
      setModal(null);
      await loadData();
    } catch (error) {
      notify("error", getErrorMessage(error));
    } finally {
      setSaving(false);
    }
  };

  const deactivate = async (item) => {
    if (!window.confirm(`Deactivate "${item.name || item.sku || item.code}"?`)) return;
    try {
      await api.delete(`${CONFIG[page].endpoint}/${item.id}`);
      notify("success", "Record deactivated.");
      loadData();
    } catch (error) { notify("error", getErrorMessage(error)); }
  };

  const submitMovement = async (event) => {
    event.preventDefault();
    const payload = {
      productId: Number(form.productId),
      locationId: Number(form.locationId),
      movementType: form.movementType,
      quantity: Number(form.quantity),
      referenceNumber: form.referenceNumber,
      reason: form.reason,
    };
    setSaving(true);
    try {
      await api.post("/inventory/movements", payload);
      notify("success", "Inventory movement recorded.");
      setModal(null);
      await loadData();
    } catch (error) { notify("error", getErrorMessage(error)); }
    finally { setSaving(false); }
  };

  const filtered = useMemo(() => {
    if (!currentConfig) return [];
    const rows = data[page] || [];
    const q = query.trim().toLowerCase();
    if (!q) return rows;
    return rows.filter((item) => Object.values(item).some((value) => String(value ?? "").toLowerCase().includes(q)));
  }, [currentConfig, data, page, query]);

  const stats = [
    { label: "Total Products", value: data.products.length, icon: Package, color: "blue", note: "Product master records" },
    { label: "Warehouses", value: data.warehouses.length, icon: Building2, color: "violet", note: "Registered facilities" },
    { label: "Storage Locations", value: data.locations.length, icon: MapPin, color: "orange", note: "Defined storage areas" },
    { label: "Stock Records", value: data.balances.length, icon: Boxes, color: "green", note: "Product-location balances" },
  ];

  return (
    <div className="app-shell">
      {sidebarOpen && <button className="mobile-scrim" aria-label="Close menu" onClick={() => setSidebarOpen(false)} />}
      <aside className={`sidebar ${sidebarOpen ? "sidebar-open" : ""}`}>
        <div className="brand">
          <div className="brand-mark"><Boxes size={22} /></div>
          <div><strong>coreFlow</strong><span>INVENTORY ERP</span></div>
          <button className="icon-btn sidebar-close" onClick={() => setSidebarOpen(false)} aria-label="Close sidebar"><X size={18} /></button>
        </div>
        <div className="workspace"><span className="workspace-icon">S</span><div><strong>Samson Holdings</strong><small>Operations workspace</small></div><ChevronDown size={15} /></div>
        <p className="nav-caption">WORKSPACE</p>
        <nav className="nav-list">
          {NAV.map(({ id, label, icon: Icon }) => (
            <button key={id} className={`nav-item ${page === id ? "active" : ""}`} onClick={() => { setPage(id); setQuery(""); setSidebarOpen(false); }}>
              <Icon size={18} strokeWidth={1.8} /><span>{label}</span>{id === "inventory" && <span className="nav-dot" />}
            </button>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <div className="help-card"><div className="help-icon"><CircleHelp size={17} /></div><strong>Need a hand?</strong><p>Keep your inventory accurate and organized.</p></div>
          <button className="nav-item" onClick={() => notify("info", "Settings and access control are planned for a future phase.")}><Settings2 size={18} /><span>Settings</span></button>
          <div className="profile"><div className="avatar">R</div><div><strong>ERP Administrator</strong><small>Local demo session</small></div><ChevronDown size={16} /></div>
        </div>
      </aside>

      <main className="main-area">
        <header className="topbar">
          <div className="topbar-left">
            <button className="icon-btn menu-btn" onClick={() => setSidebarOpen(true)} aria-label="Open menu"><Menu size={21} /></button>
            <div className="breadcrumbs"><span>Workspace</span><span className="crumb-sep">/</span><strong>{NAV.find((n) => n.id === page)?.label}</strong></div>
          </div>
          <div className="topbar-right"><span className="system-status"><span /> System workspace</span><button className="icon-btn refresh-btn" onClick={loadData} aria-label="Refresh data"><RefreshCw size={17} /></button><div className="avatar top-avatar">R</div></div>
        </header>

        <div className="page-content">
          {page === "dashboard" ? (
            <Dashboard data={data} loading={loading} onNavigate={setPage} onRefresh={loadData} />
          ) : page === "inventory" ? (
            <InventoryPage data={data} loading={loading} query={query} setQuery={setQuery} onRefresh={loadData} onAdd={() => { setForm({ productId: "", locationId: "", movementType: "RECEIPT", quantity: "", referenceNumber: "", reason: "" }); setModal({ kind: "movement", page: "inventory" }); }} />
          ) : (
            <MasterPage config={currentConfig} rows={filtered} loading={loading} query={query} setQuery={setQuery} onAdd={openCreate} onEdit={openEdit} onDeactivate={deactivate} />
          )}
        </div>
      </main>

      {notice && <div className={`toast toast-${notice.type}`}><span className="toast-symbol">{notice.type === "success" ? <Check size={17} /> : notice.type === "error" ? <X size={17} /> : <Activity size={17} />}</span><span>{notice.message}</span><button onClick={() => setNotice(null)} aria-label="Dismiss"><X size={15} /></button></div>}

      {modal && modal.kind !== "movement" && (
        <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && setModal(null)}>
          <form className="modal" onSubmit={saveMaster}>
            <div className="modal-header"><div><span className="eyebrow">{modal.kind === "edit" ? "UPDATE RECORD" : "NEW RECORD"}</span><h2>{modal.kind === "edit" ? "Edit " : "Create "}{CONFIG[modal.page].singular}</h2><p>Enter the details below to {modal.kind === "edit" ? "update this record" : "add a new record"}.</p></div><button type="button" className="icon-btn" onClick={() => setModal(null)}><X size={19} /></button></div>
            <div className="form-grid">
              {CONFIG[modal.page].fields.map((field) => <Field key={field.name} field={field} value={form[field.name]} onChange={(value) => setForm((old) => ({ ...old, [field.name]: value }))} data={data} />)}
            </div>
            <div className="modal-footer"><button type="button" className="btn btn-light" onClick={() => setModal(null)}>Cancel</button><button type="submit" className="btn btn-primary" disabled={saving}>{saving ? "Saving..." : modal.kind === "edit" ? "Save Changes" : `Create ${CONFIG[modal.page].singular}`}</button></div>
          </form>
        </div>
      )}
      {modal?.kind === "movement" && (
        <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && setModal(null)}>
          <form className="modal" onSubmit={submitMovement}>
            <div className="modal-header"><div><span className="eyebrow">INVENTORY CONTROL</span><h2>Record stock movement</h2><p>Choose the product, storage location and movement details.</p></div><button type="button" className="icon-btn" onClick={() => setModal(null)}><X size={19} /></button></div>
            <div className="form-grid">
              <Field field={{ name: "productId", label: "Product", type: "select", source: "products", required: true }} value={form.productId} onChange={(v) => setForm((o) => ({ ...o, productId: v }))} data={data} />
              <Field field={{ name: "locationId", label: "Storage Location", type: "select", source: "locations", required: true }} value={form.locationId} onChange={(v) => setForm((o) => ({ ...o, locationId: v }))} data={data} />
              <Field field={{ name: "movementType", label: "Movement Type", type: "selectStatic", options: ["RECEIPT", "ISSUE", "ADJUSTMENT_IN", "ADJUSTMENT_OUT"], required: true }} value={form.movementType} onChange={(v) => setForm((o) => ({ ...o, movementType: v }))} data={data} />
              <Field field={{ name: "quantity", label: "Quantity", type: "number", required: true }} value={form.quantity} onChange={(v) => setForm((o) => ({ ...o, quantity: v }))} data={data} />
              <Field field={{ name: "referenceNumber", label: "Reference Number" }} value={form.referenceNumber} onChange={(v) => setForm((o) => ({ ...o, referenceNumber: v }))} data={data} />
              <Field field={{ name: "reason", label: "Reason", type: "textarea", required: true }} value={form.reason} onChange={(v) => setForm((o) => ({ ...o, reason: v }))} data={data} />
            </div>
            <div className="notice-box"><Activity size={17} /><span>Stock decreases cannot exceed the available balance. Only record quantities that have been verified.</span></div>
            <div className="modal-footer"><button type="button" className="btn btn-light" onClick={() => setModal(null)}>Cancel</button><button type="submit" className="btn btn-primary" disabled={saving}>{saving ? "Recording..." : "Record Movement"}</button></div>
          </form>
        </div>
      )}
    </div>
  );
}

function Field({ field, value, onChange, data }) {
  const options = field.type === "select" ? (data[field.source] || []) : [];
  const getOption = (item) => ({
    id: item.id,
    label: item.name ? `${item.name}${item.code ? ` (${item.code})` : ""}` : item.sku || item.code || `#${item.id}`,
  });
  return (
    <label className={`form-field ${field.type === "textarea" ? "field-full" : ""} ${field.type === "checkbox" ? "field-checkbox" : ""}`}>
      {field.type === "checkbox" ? <>
        <input type="checkbox" checked={Boolean(value)} onChange={(e) => onChange(e.target.checked)} />
        <span>{field.label}</span>
      </> : <>
        <span className="field-label">{field.label}{field.required && <b> *</b>}</span>
        {field.type === "textarea" ? <textarea value={value ?? ""} required={field.required} rows={3} onChange={(e) => onChange(e.target.value)} placeholder={`Enter ${field.label.toLowerCase()}`} /> :
          field.type === "select" || field.type === "selectStatic" ? <select value={value ?? ""} required={field.required} onChange={(e) => onChange(e.target.value)}><option value="">Select {field.label.toLowerCase()}</option>{field.type === "select" ? options.map((item) => { const option = getOption(item); return <option key={option.id} value={option.id}>{option.label}</option>; }) : field.options.map((option) => <option key={option} value={option}>{labelize(option)}</option>)}</select> :
            <input type={field.type === "number" ? "number" : "text"} min={field.type === "number" ? "0" : undefined} step={field.type === "number" ? "0.001" : undefined} required={field.required} value={value ?? ""} onChange={(e) => onChange(e.target.value)} placeholder={`Enter ${field.label.toLowerCase()}`} />}
      </>}
    </label>
  );
}

function PageHeading({ eyebrow, title, description, action, onAction }) {
  return <div className="page-heading"><div><span className="eyebrow">{eyebrow}</span><h1>{title}</h1><p>{description}</p></div>{action && <button className="btn btn-primary" onClick={onAction}><Plus size={17} />{action}</button>}</div>;
}

function Dashboard({ data, loading, onNavigate, onRefresh }) {
  const recent = [...(data.transactions || [])].sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0)).slice(0, 6);
  const totalQty = (data.balances || []).reduce((sum, item) => sum + Number(item.quantity || 0), 0);
  return <>
    <PageHeading eyebrow="OVERVIEW" title="Good to see you." description="Here’s what’s happening across your inventory workspace." action="Refresh data" onAction={onRefresh} />
    <div className="welcome-banner"><div className="welcome-copy"><span className="banner-tag">CORE FLOW · INVENTORY ERP</span><h2>Your operations, all in one flow.</h2><p>Manage products, organize storage and keep stock movements traceable from one place.</p><button onClick={() => onNavigate("inventory")} className="banner-button">Manage inventory <ArrowUpFromLine size={16} /></button></div><div className="banner-art"><div className="art-circle circle-one" /><div className="art-circle circle-two" /><div className="art-box"><Boxes size={72} strokeWidth={1.2} /></div><div className="art-chip"><Activity size={16} /> Stock control</div></div></div>
    <div className="section-head"><div><h2>Workspace overview</h2><p>A quick snapshot of your master data and stock records.</p></div><span className="updated-label"><span /> Live API data</span></div>
    <div className="stats-grid">{[
      ...[
        { label: "Total Products", value: data.products.length, icon: Package, color: "blue", note: "Product master records" },
        { label: "Warehouses", value: data.warehouses.length, icon: Building2, color: "violet", note: "Registered facilities" },
        { label: "Storage Locations", value: data.locations.length, icon: MapPin, color: "orange", note: "Defined storage areas" },
        { label: "Total Stock Quantity", value: totalQty.toLocaleString(), icon: Boxes, color: "green", note: "Sum of balance quantities" },
      ],
    ].map((stat) => <div className="stat-card" key={stat.label}><div className={`stat-icon ${stat.color}`}><stat.icon size={20} /></div><span className="stat-label">{stat.label}</span><strong>{loading ? "—" : stat.value}</strong><small>{stat.note}</small></div>)}</div>
    <div className="dashboard-grid">
      <section className="panel recent-panel"><div className="panel-heading"><div><h2>Recent stock movements</h2><p>Latest inventory activity recorded in the system.</p></div><button className="text-button" onClick={() => onNavigate("inventory")}>View inventory <span>→</span></button></div>
        {recent.length ? <div className="table-wrap"><table><thead><tr><th>Movement</th><th>Product</th><th>Quantity</th><th>When</th></tr></thead><tbody>{recent.map((tx) => <tr key={tx.id}><td><span className={`movement-pill ${String(tx.movementType).includes("OUT") || tx.movementType === "ISSUE" ? "movement-out" : "movement-in"}`}>{labelize(tx.movementType)}</span></td><td><strong>{tx.productName || tx.product?.name || `Product #${tx.productId || tx.product?.id || "—"}`}</strong><small className="cell-sub">{tx.referenceNumber || "No reference"}</small></td><td className="quantity-cell">{Number(tx.quantity || 0).toLocaleString()}</td><td>{tx.createdAt ? new Date(tx.createdAt).toLocaleDateString() : "—"}</td></tr>)}</tbody></table></div> : <EmptyState title="No stock movements yet" text="When stock movements are recorded, they will appear here." />}
      </section>
      <section className="panel quick-panel"><div className="panel-heading"><div><h2>Quick actions</h2><p>Jump into a common task.</p></div></div><div className="quick-actions">
        <button onClick={() => onNavigate("products")}><span className="quick-icon q-blue"><Package size={18} /></span><span><strong>Manage products</strong><small>View and maintain product master</small></span><span className="quick-arrow">→</span></button>
        <button onClick={() => onNavigate("warehouses")}><span className="quick-icon q-violet"><Building2 size={18} /></span><span><strong>Manage warehouses</strong><small>Organize storage facilities</small></span><span className="quick-arrow">→</span></button>
        <button onClick={() => onNavigate("locations")}><span className="quick-icon q-orange"><MapPin size={18} /></span><span><strong>Storage locations</strong><small>Manage warehouse locations</small></span><span className="quick-arrow">→</span></button>
        <button onClick={() => onNavigate("inventory")}><span className="quick-icon q-green"><ArrowDownToLine size={18} /></span><span><strong>Stock movement</strong><small>Record a receipt, issue or adjustment</small></span><span className="quick-arrow">→</span></button>
      </div></section>
    </div>
    <div className="dashboard-footnote"><Activity size={16} /><span>Dashboard metrics are calculated from data returned by the backend APIs. Empty values may mean the database has no records yet or an API needs attention.</span></div>
  </>;
}

function MasterPage({ config, rows, loading, query, setQuery, onAdd, onEdit, onDeactivate }) {
  return <>
    <PageHeading eyebrow="MASTER DATA" title={config.title} description={config.description} action={`Add ${config.singular}`} onAction={onAdd} />
    <div className="panel data-panel"><div className="table-toolbar"><div className="search-box"><Search size={17} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder={`Search ${config.title.toLowerCase()}...`} /></div><div className="toolbar-right"><span className="record-count">{rows.length} records</span><span className="filter-chip"><Activity size={14} /> All records</span></div></div>
      {loading ? <LoadingState /> : rows.length ? <div className="table-wrap"><table><thead><tr>{config.columns.map(([key, title]) => <th key={key}>{title}</th>)}<th className="actions-heading">Actions</th></tr></thead><tbody>{rows.map((item) => <tr key={item.id}>{config.columns.map(([key]) => <td key={key}>{key === "active" ? <span className={`status-pill ${item[key] ? "status-active" : "status-inactive"}`}><span />{item[key] ? "Active" : "Inactive"}</span> : key.toLowerCase().includes("price") ? money(item[key]) : <span className={key === "name" || key === "sku" ? "primary-cell" : ""}>{item[key] == null || item[key] === "" ? "—" : String(item[key])}</span>}</td>)}<td><div className="row-actions"><button className="small-action" onClick={() => onEdit(item)}>Edit</button><button className="small-action danger-action" onClick={() => onDeactivate(item)}>Deactivate</button></div></td></tr>)}</tbody></table></div> : <EmptyState title={`No ${config.title.toLowerCase()} found`} text={query ? "Try another search term." : `Create your first ${config.singular.toLowerCase()} to get started.`} action={!query ? `Add ${config.singular}` : null} onAction={onAdd} />}
      <div className="table-footer"><span>Showing {rows.length} record{rows.length === 1 ? "" : "s"}</span><span>CoreFlow ERP · Master data</span></div>
    </div>
  </>;
}

function InventoryPage({ data, loading, query, setQuery, onRefresh, onAdd }) {
  const [tab, setTab] = useState("balances");
  const rows = tab === "balances" ? data.balances : data.transactions;
  const filtered = rows.filter((item) => Object.values(item).some((v) => String(v ?? "").toLowerCase().includes(query.toLowerCase())));
  return <>
    <PageHeading eyebrow="STOCK CONTROL" title="Inventory" description="Monitor stock balances and trace every recorded stock movement." action="Record movement" onAction={onAdd} />
    <div className="stats-grid inventory-stats">
      <div className="stat-card"><div className="stat-icon blue"><Boxes size={20} /></div><span className="stat-label">Balance records</span><strong>{data.balances.length}</strong><small>Product-location balances</small></div>
      <div className="stat-card"><div className="stat-icon green"><ArrowDownToLine size={20} /></div><span className="stat-label">Transactions</span><strong>{data.transactions.length}</strong><small>Recorded stock movements</small></div>
      <div className="stat-card"><div className="stat-icon violet"><Package size={20} /></div><span className="stat-label">Products tracked</span><strong>{new Set(data.balances.map((b) => b.productId ?? b.product?.id)).size}</strong><small>Products with balance records</small></div>
    </div>
    <div className="panel data-panel"><div className="inventory-tabs"><button className={tab === "balances" ? "selected" : ""} onClick={() => { setTab("balances"); setQuery(""); }}>Stock balances <span>{data.balances.length}</span></button><button className={tab === "transactions" ? "selected" : ""} onClick={() => { setTab("transactions"); setQuery(""); }}>Transaction history <span>{data.transactions.length}</span></button><button className="icon-btn tab-refresh" onClick={onRefresh} aria-label="Refresh inventory"><RefreshCw size={16} /></button></div>
      <div className="table-toolbar"><div className="search-box"><Search size={17} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder={tab === "balances" ? "Search balances..." : "Search transactions..."} /></div><span className="record-count">{filtered.length} records</span></div>
      {loading ? <LoadingState /> : filtered.length ? <div className="table-wrap"><table><thead>{tab === "balances" ? <tr><th>Product</th><th>Storage Location</th><th>Quantity</th><th>Last Updated</th></tr> : <tr><th>Movement</th><th>Product</th><th>Location</th><th>Quantity</th><th>Balance After</th><th>Reference</th><th>Date</th></tr>}</thead><tbody>{filtered.map((item, i) => tab === "balances" ? <tr key={item.id ?? i}><td><strong>{item.productName || item.product?.name || `Product #${item.productId ?? item.product?.id ?? "—"}`}</strong><small className="cell-sub">{item.sku || item.product?.sku || ""}</small></td><td>{item.locationName || item.location?.name || `Location #${item.locationId ?? item.location?.id ?? "—"}`}</td><td><strong className="quantity-cell">{Number(item.quantity || 0).toLocaleString()}</strong></td><td>{item.updatedAt ? new Date(item.updatedAt).toLocaleString() : "—"}</td></tr> : <tr key={item.id ?? i}><td><span className={`movement-pill ${String(item.movementType).includes("OUT") || item.movementType === "ISSUE" ? "movement-out" : "movement-in"}`}>{labelize(item.movementType)}</span></td><td>{item.productName || item.product?.name || `#${item.productId ?? item.product?.id ?? "—"}`}</td><td>{item.locationName || item.location?.name || `#${item.locationId ?? item.location?.id ?? "—"}`}</td><td className="quantity-cell">{Number(item.quantity || 0).toLocaleString()}</td><td>{item.balanceAfter == null ? "—" : Number(item.balanceAfter).toLocaleString()}</td><td>{item.referenceNumber || "—"}</td><td>{item.createdAt ? new Date(item.createdAt).toLocaleString() : "—"}</td></tr>)}</tbody></table></div> : <EmptyState title={tab === "balances" ? "No stock balances yet" : "No transactions yet"} text={query ? "Try another search term." : "Once inventory movements are recorded, data will appear here."} action={tab === "transactions" ? "Record movement" : null} onAction={onAdd} />}
      <div className="table-footer"><span>{tab === "balances" ? "Current stock position" : "Immutable movement history"}</span><span>CoreFlow ERP · Inventory</span></div>
    </div>
  </>;
}

function EmptyState({ title, text, action, onAction }) {
  return <div className="empty-state"><div className="empty-icon"><Archive size={23} /></div><h3>{title}</h3><p>{text}</p>{action && <button className="btn btn-primary" onClick={onAction}><Plus size={16} />{action}</button>}</div>;
}
function LoadingState() { return <div className="loading-state"><span className="spinner" />Loading records…</div>; }

export default App;
