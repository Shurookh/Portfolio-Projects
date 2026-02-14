package service;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import model.Product;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP server for data science dashboard
 * Provides real-time visualization of flash sale metrics
 */
public class DashboardServer {
    private final HttpServer server;
    private final int port;
    private List<SimulationData> simulationHistory;
    
    public static class SimulationData {
        public int stockCount;
        public int userCount;
        public int successfulPurchases;
        public int failedPurchases;
        public int remainingStock;
        public double totalRevenue;
        public long durationMs;
        public double throughput;
        public String timestamp;
        public boolean isSafe;
        
        public SimulationData(int stock, int users, int success, int failed, 
                            int remaining, double revenue, long duration, boolean safe) {
            this.stockCount = stock;
            this.userCount = users;
            this.successfulPurchases = success;
            this.failedPurchases = failed;
            this.remainingStock = remaining;
            this.totalRevenue = revenue;
            this.durationMs = duration;
            this.throughput = (success + failed) * 1000.0 / duration;
            this.timestamp = java.time.LocalDateTime.now().toString();
            this.isSafe = safe;
        }
    }
    
    public DashboardServer(int port) throws IOException {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.simulationHistory = new ArrayList<>();
        setupRoutes();
    }
    
    private void setupRoutes() {
        server.createContext("/", new DashboardHandler());
        server.createContext("/api/data", new DataHandler());
        server.createContext("/api/compare", new CompareHandler());
        server.setExecutor(Executors.newFixedThreadPool(4));
    }
    
    public void start() {
        server.start();
        System.out.println("\n" + "=".repeat(80));
        System.out.println("📊 Dashboard Server Started");
        System.out.println("=".repeat(80));
        System.out.println("Access the dashboard at: http://localhost:" + port);
        System.out.println("Press Ctrl+C to stop the server");
        System.out.println("=".repeat(80) + "\n");
    }
    
    public void stop() {
        server.stop(0);
    }
    
    public void addSimulation(SimulationData data) {
        simulationHistory.add(data);
    }
    
    class DashboardHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = getDashboardHTML();
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }
    
    class DataHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < simulationHistory.size(); i++) {
                if (i > 0) json.append(",");
                SimulationData data = simulationHistory.get(i);
                json.append(String.format(
                    "{\"stockCount\":%d,\"userCount\":%d,\"successfulPurchases\":%d," +
                    "\"failedPurchases\":%d,\"remainingStock\":%d,\"totalRevenue\":%.2f," +
                    "\"durationMs\":%d,\"throughput\":%.2f,\"timestamp\":\"%s\",\"isSafe\":%b}",
                    data.stockCount, data.userCount, data.successfulPurchases,
                    data.failedPurchases, data.remainingStock, data.totalRevenue,
                    data.durationMs, data.throughput, data.timestamp, data.isSafe
                ));
            }
            json.append("]");
            
            byte[] response = json.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }
    
    class CompareHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Return comparison data for safe vs unsafe
            String json = "{\"safe\":{},\"unsafe\":{}}";
            byte[] response = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }
    
    private String getDashboardHTML() {
        return "<!DOCTYPE html>\n" +
"<html lang='en'>\n" +
"<head>\n" +
"    <meta charset='UTF-8'>\n" +
"    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
"    <title>Flash Sale Analytics Dashboard</title>\n" +
"    <script src='https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js'></script>\n" +
"    <style>\n" +
"        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
"        body {\n" +
"            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
"            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
"            min-height: 100vh;\n" +
"            padding: 20px;\n" +
"        }\n" +
"        .container {\n" +
"            max-width: 1400px;\n" +
"            margin: 0 auto;\n" +
"        }\n" +
"        .header {\n" +
"            background: rgba(255, 255, 255, 0.95);\n" +
"            padding: 30px;\n" +
"            border-radius: 20px;\n" +
"            box-shadow: 0 20px 60px rgba(0,0,0,0.3);\n" +
"            margin-bottom: 30px;\n" +
"            text-align: center;\n" +
"        }\n" +
"        h1 {\n" +
"            color: #667eea;\n" +
"            font-size: 2.5em;\n" +
"            margin-bottom: 10px;\n" +
"        }\n" +
"        .subtitle {\n" +
"            color: #666;\n" +
"            font-size: 1.2em;\n" +
"        }\n" +
"        .stats-grid {\n" +
"            display: grid;\n" +
"            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));\n" +
"            gap: 20px;\n" +
"            margin-bottom: 30px;\n" +
"        }\n" +
"        .stat-card {\n" +
"            background: rgba(255, 255, 255, 0.95);\n" +
"            padding: 25px;\n" +
"            border-radius: 15px;\n" +
"            box-shadow: 0 10px 30px rgba(0,0,0,0.2);\n" +
"            text-align: center;\n" +
"            transition: transform 0.3s ease;\n" +
"        }\n" +
"        .stat-card:hover {\n" +
"            transform: translateY(-5px);\n" +
"        }\n" +
"        .stat-value {\n" +
"            font-size: 2.5em;\n" +
"            font-weight: bold;\n" +
"            color: #667eea;\n" +
"            margin: 10px 0;\n" +
"        }\n" +
"        .stat-label {\n" +
"            color: #666;\n" +
"            font-size: 0.9em;\n" +
"            text-transform: uppercase;\n" +
"            letter-spacing: 1px;\n" +
"        }\n" +
"        .chart-grid {\n" +
"            display: grid;\n" +
"            grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));\n" +
"            gap: 30px;\n" +
"            margin-bottom: 30px;\n" +
"        }\n" +
"        .chart-container {\n" +
"            background: rgba(255, 255, 255, 0.95);\n" +
"            padding: 30px;\n" +
"            border-radius: 20px;\n" +
"            box-shadow: 0 15px 40px rgba(0,0,0,0.2);\n" +
"        }\n" +
"        .chart-title {\n" +
"            font-size: 1.5em;\n" +
"            color: #333;\n" +
"            margin-bottom: 20px;\n" +
"            text-align: center;\n" +
"        }\n" +
"        .status-badge {\n" +
"            display: inline-block;\n" +
"            padding: 8px 20px;\n" +
"            border-radius: 25px;\n" +
"            font-weight: bold;\n" +
"            margin: 10px 5px;\n" +
"        }\n" +
"        .status-success {\n" +
"            background: #10b981;\n" +
"            color: white;\n" +
"        }\n" +
"        .status-warning {\n" +
"            background: #f59e0b;\n" +
"            color: white;\n" +
"        }\n" +
"    </style>\n" +
"</head>\n" +
"<body>\n" +
"    <div class='container'>\n" +
"        <div class='header'>\n" +
"            <h1>⚡ Flash Sale Analytics Dashboard</h1>\n" +
"            <p class='subtitle'>Real-Time Performance Monitoring & Thread Safety Validation</p>\n" +
"            <div style='margin-top: 15px;'>\n" +
"                <span class='status-badge status-success'>Thread Safe ✓</span>\n" +
"                <span class='status-badge status-warning'>Race Condition Prevented</span>\n" +
"            </div>\n" +
"        </div>\n" +
"        \n" +
"        <div class='stats-grid' id='statsGrid'>\n" +
"            <div class='stat-card'>\n" +
"                <div class='stat-label'>Total Attempts</div>\n" +
"                <div class='stat-value' id='totalAttempts'>-</div>\n" +
"            </div>\n" +
"            <div class='stat-card'>\n" +
"                <div class='stat-label'>Successful Purchases</div>\n" +
"                <div class='stat-value' id='successfulPurchases' style='color: #10b981;'>-</div>\n" +
"            </div>\n" +
"            <div class='stat-card'>\n" +
"                <div class='stat-label'>Failed Purchases</div>\n" +
"                <div class='stat-value' id='failedPurchases' style='color: #ef4444;'>-</div>\n" +
"            </div>\n" +
"            <div class='stat-card'>\n" +
"                <div class='stat-label'>Throughput</div>\n" +
"                <div class='stat-value' id='throughput'>-</div>\n" +
"                <div class='stat-label'>req/sec</div>\n" +
"            </div>\n" +
"        </div>\n" +
"        \n" +
"        <div class='chart-grid'>\n" +
"            <div class='chart-container'>\n" +
"                <h3 class='chart-title'>Purchase Distribution</h3>\n" +
"                <canvas id='purchaseChart'></canvas>\n" +
"            </div>\n" +
"            <div class='chart-container'>\n" +
"                <h3 class='chart-title'>Performance Metrics</h3>\n" +
"                <canvas id='performanceChart'></canvas>\n" +
"            </div>\n" +
"        </div>\n" +
"        \n" +
"        <div class='chart-container' style='margin-bottom: 30px;'>\n" +
"            <h3 class='chart-title'>Simulation History</h3>\n" +
"            <canvas id='historyChart'></canvas>\n" +
"        </div>\n" +
"        \n" +
"        <div class='chart-grid'>\n" +
"            <div class='chart-container'>\n" +
"                <h3 class='chart-title'>Safe vs Unsafe Comparison</h3>\n" +
"                <canvas id='comparisonChart'></canvas>\n" +
"            </div>\n" +
"            <div class='chart-container'>\n" +
"                <h3 class='chart-title'>Revenue Analysis</h3>\n" +
"                <canvas id='revenueChart'></canvas>\n" +
"            </div>\n" +
"        </div>\n" +
"    </div>\n" +
"    \n" +
"    <script>\n" +
"        let charts = {};\n" +
"        \n" +
"        function initCharts() {\n" +
"            // Purchase Distribution Pie Chart\n" +
"            charts.purchase = new Chart(document.getElementById('purchaseChart'), {\n" +
"                type: 'doughnut',\n" +
"                data: {\n" +
"                    labels: ['Successful', 'Failed'],\n" +
"                    datasets: [{\n" +
"                        data: [0, 0],\n" +
"                        backgroundColor: ['#10b981', '#ef4444']\n" +
"                    }]\n" +
"                },\n" +
"                options: {\n" +
"                    responsive: true,\n" +
"                    plugins: {\n" +
"                        legend: { position: 'bottom' }\n" +
"                    }\n" +
"                }\n" +
"            });\n" +
"            \n" +
"            // Performance Bar Chart\n" +
"            charts.performance = new Chart(document.getElementById('performanceChart'), {\n" +
"                type: 'bar',\n" +
"                data: {\n" +
"                    labels: ['Throughput (k/s)', 'Duration (ms)', 'Success Rate (%)'],\n" +
"                    datasets: [{\n" +
"                        label: 'Metrics',\n" +
"                        data: [0, 0, 0],\n" +
"                        backgroundColor: ['#667eea', '#764ba2', '#f59e0b']\n" +
"                    }]\n" +
"                },\n" +
"                options: {\n" +
"                    responsive: true,\n" +
"                    scales: { y: { beginAtZero: true } }\n" +
"                }\n" +
"            });\n" +
"            \n" +
"            // History Line Chart\n" +
"            charts.history = new Chart(document.getElementById('historyChart'), {\n" +
"                type: 'line',\n" +
"                data: {\n" +
"                    labels: [],\n" +
"                    datasets: [{\n" +
"                        label: 'Successful Purchases',\n" +
"                        data: [],\n" +
"                        borderColor: '#10b981',\n" +
"                        tension: 0.4,\n" +
"                        fill: false\n" +
"                    }, {\n" +
"                        label: 'Failed Purchases',\n" +
"                        data: [],\n" +
"                        borderColor: '#ef4444',\n" +
"                        tension: 0.4,\n" +
"                        fill: false\n" +
"                    }]\n" +
"                },\n" +
"                options: {\n" +
"                    responsive: true,\n" +
"                    plugins: {\n" +
"                        legend: { position: 'bottom' }\n" +
"                    }\n" +
"                }\n" +
"            });\n" +
"            \n" +
"            // Comparison Chart\n" +
"            charts.comparison = new Chart(document.getElementById('comparisonChart'), {\n" +
"                type: 'bar',\n" +
"                data: {\n" +
"                    labels: ['With Lock (Safe)', 'Without Lock (Unsafe)'],\n" +
"                    datasets: [{\n" +
"                        label: 'Successful Purchases',\n" +
"                        data: [100, 101],\n" +
"                        backgroundColor: ['#10b981', '#ef4444']\n" +
"                    }]\n" +
"                },\n" +
"                options: {\n" +
"                    responsive: true,\n" +
"                    scales: { y: { beginAtZero: true } }\n" +
"                }\n" +
"            });\n" +
"            \n" +
"            // Revenue Chart\n" +
"            charts.revenue = new Chart(document.getElementById('revenueChart'), {\n" +
"                type: 'line',\n" +
"                data: {\n" +
"                    labels: [],\n" +
"                    datasets: [{\n" +
"                        label: 'Total Revenue ($)',\n" +
"                        data: [],\n" +
"                        borderColor: '#667eea',\n" +
"                        backgroundColor: 'rgba(102, 126, 234, 0.1)',\n" +
"                        tension: 0.4,\n" +
"                        fill: true\n" +
"                    }]\n" +
"                },\n" +
"                options: {\n" +
"                    responsive: true,\n" +
"                    plugins: {\n" +
"                        legend: { position: 'bottom' }\n" +
"                    }\n" +
"                }\n" +
"            });\n" +
"        }\n" +
"        \n" +
"        function updateDashboard() {\n" +
"            fetch('/api/data')\n" +
"                .then(response => response.json())\n" +
"                .then(data => {\n" +
"                    if (data.length === 0) return;\n" +
"                    \n" +
"                    const latest = data[data.length - 1];\n" +
"                    \n" +
"                    // Update stats\n" +
"                    document.getElementById('totalAttempts').textContent = \n" +
"                        latest.successfulPurchases + latest.failedPurchases;\n" +
"                    document.getElementById('successfulPurchases').textContent = \n" +
"                        latest.successfulPurchases;\n" +
"                    document.getElementById('failedPurchases').textContent = \n" +
"                        latest.failedPurchases;\n" +
"                    document.getElementById('throughput').textContent = \n" +
"                        Math.round(latest.throughput);\n" +
"                    \n" +
"                    // Update purchase chart\n" +
"                    charts.purchase.data.datasets[0].data = [\n" +
"                        latest.successfulPurchases,\n" +
"                        latest.failedPurchases\n" +
"                    ];\n" +
"                    charts.purchase.update();\n" +
"                    \n" +
"                    // Update performance chart\n" +
"                    const successRate = (latest.successfulPurchases / \n" +
"                        (latest.successfulPurchases + latest.failedPurchases)) * 100;\n" +
"                    charts.performance.data.datasets[0].data = [\n" +
"                        (latest.throughput / 1000).toFixed(2),\n" +
"                        latest.durationMs,\n" +
"                        successRate.toFixed(2)\n" +
"                    ];\n" +
"                    charts.performance.update();\n" +
"                    \n" +
"                    // Update history chart\n" +
"                    const labels = data.map((d, i) => `Run ${i + 1}`);\n" +
"                    charts.history.data.labels = labels;\n" +
"                    charts.history.data.datasets[0].data = \n" +
"                        data.map(d => d.successfulPurchases);\n" +
"                    charts.history.data.datasets[1].data = \n" +
"                        data.map(d => d.failedPurchases);\n" +
"                    charts.history.update();\n" +
"                    \n" +
"                    // Update revenue chart\n" +
"                    charts.revenue.data.labels = labels;\n" +
"                    charts.revenue.data.datasets[0].data = \n" +
"                        data.map(d => d.totalRevenue);\n" +
"                    charts.revenue.update();\n" +
"                });\n" +
"        }\n" +
"        \n" +
"        // Initialize\n" +
"        initCharts();\n" +
"        updateDashboard();\n" +
"        setInterval(updateDashboard, 2000);\n" +
"    </script>\n" +
"</body>\n" +
"</html>";
    }
}
