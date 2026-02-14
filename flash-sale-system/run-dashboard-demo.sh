#!/bin/bash

# Script to run dashboard and generate sample data for visualization

echo "=================================="
echo "Flash Sale Dashboard Demo"
echo "=================================="
echo ""

# Start the dashboard in background
cd /app/flash-sale-system

# Create a simple automation script
cat > /tmp/demo_input.txt << 'EOF'
4
6
EOF

echo "Starting dashboard server and running simulations..."
echo ""

# Run with automation
java -cp bin MainWithDashboard < /tmp/demo_input.txt &
DASHBOARD_PID=$!

# Wait for server to start
sleep 3

echo ""
echo "=================================="
echo "Dashboard is now running!"
echo "=================================="
echo ""
echo "🌐 Access the dashboard at:"
echo "   http://localhost:8080"
echo ""
echo "The dashboard shows:"
echo "  ✓ Real-time statistics"
echo "  ✓ Purchase distribution charts"
echo "  ✓ Performance metrics"
echo "  ✓ Simulation history"
echo "  ✓ Safe vs Unsafe comparison"
echo "  ✓ Revenue analysis"
echo ""
echo "Dashboard will run for 60 seconds..."
echo "Press Ctrl+C to stop early"
echo ""

# Keep it running
sleep 60

# Cleanup
kill $DASHBOARD_PID 2>/dev/null

echo ""
echo "Demo completed!"
