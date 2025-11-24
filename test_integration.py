#!/usr/bin/env python3
"""
Manual integration test for the Telegram Login API.
This script tests the API endpoints using direct HTTP requests.
"""

import subprocess
import time
import requests
import sys


def test_api():
    """Test the API endpoints."""
    base_url = "http://localhost:8000"
    
    print("Testing Telegram Login API...")
    print("=" * 50)
    
    # Test 1: Root endpoint
    print("\n✓ Test 1: Root endpoint")
    response = requests.get(f"{base_url}/")
    assert response.status_code == 200
    data = response.json()
    assert "message" in data
    assert data["message"] == "Telegram Login API"
    print(f"  Response: {data}")
    
    # Test 2: Health endpoint
    print("\n✓ Test 2: Health endpoint")
    response = requests.get(f"{base_url}/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    print(f"  Response: {data}")
    
    # Test 3: List sessions (should be empty)
    print("\n✓ Test 3: List sessions")
    response = requests.get(f"{base_url}/api/v1/telegram/sessions")
    assert response.status_code == 200
    data = response.json()
    assert "sessions" in data
    assert "total" in data
    assert data["total"] == 0
    print(f"  Response: {data}")
    
    # Test 4: Validation error
    print("\n✓ Test 4: Login endpoint validation")
    response = requests.post(f"{base_url}/api/v1/telegram/login", json={})
    assert response.status_code == 422  # Validation error
    print(f"  Validation error correctly returned (422)")
    
    # Test 5: Submit code to non-existent session
    print("\n✓ Test 5: Submit code to non-existent session")
    response = requests.post(f"{base_url}/api/v1/telegram/submit-code", json={
        "session_id": "nonexistent",
        "code": "12345"
    })
    assert response.status_code == 404
    print(f"  404 error correctly returned")
    
    # Test 6: Get non-existent session
    print("\n✓ Test 6: Get non-existent session")
    response = requests.get(f"{base_url}/api/v1/telegram/sessions/nonexistent")
    assert response.status_code == 404
    print(f"  404 error correctly returned")
    
    # Test 7: OpenAPI documentation
    print("\n✓ Test 7: OpenAPI documentation")
    response = requests.get(f"{base_url}/openapi.json")
    assert response.status_code == 200
    data = response.json()
    assert "openapi" in data
    assert "paths" in data
    print(f"  OpenAPI schema available with {len(data['paths'])} endpoints")
    
    print("\n" + "=" * 50)
    print("✅ All tests passed!")
    print("=" * 50)


def main():
    """Main function to run tests."""
    # Check if requests is installed
    try:
        import requests
    except ImportError:
        print("Error: 'requests' library is required")
        print("Install it with: pip install requests")
        sys.exit(1)
    
    # Start the server
    print("Starting API server...")
    server_process = subprocess.Popen(
        ["python", "run.py"],
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
    )
    
    # Wait for server to start
    print("Waiting for server to start...")
    time.sleep(5)
    
    try:
        # Run tests
        test_api()
    except AssertionError as e:
        print(f"\n❌ Test failed: {e}")
        server_process.terminate()
        sys.exit(1)
    except requests.exceptions.ConnectionError:
        print("\n❌ Could not connect to the server")
        server_process.terminate()
        sys.exit(1)
    finally:
        # Stop the server
        print("\nStopping server...")
        server_process.terminate()
        server_process.wait()


if __name__ == "__main__":
    main()
