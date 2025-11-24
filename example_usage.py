"""
Example usage of the Telegram Login API.

This script demonstrates how to use the API endpoints programmatically.
"""

import requests
import time

# API base URL
BASE_URL = "http://localhost:8000"


def login_example():
    """Example of the complete login flow."""
    
    # Step 1: Initiate login
    print("Step 1: Initiating login...")
    login_data = {
        "api_id": "YOUR_API_ID",
        "api_hash": "YOUR_API_HASH",
        "phone": "+YOUR_PHONE_NUMBER",
        "database_encryption_key": "changeme1234",
        "session_name": "example_session"
    }
    
    response = requests.post(f"{BASE_URL}/api/v1/telegram/login", json=login_data)
    result = response.json()
    print(f"Response: {result}")
    
    if not result.get("success"):
        print("Login failed!")
        return
    
    session_id = result.get("session_id")
    status = result.get("status")
    
    # Step 2: Submit code if needed
    if status == "awaiting_code":
        print("\nStep 2: Enter the code sent to your Telegram app")
        code = input("Enter code: ")
        
        code_data = {
            "session_id": session_id,
            "code": code
        }
        
        response = requests.post(
            f"{BASE_URL}/api/v1/telegram/submit-code",
            json=code_data
        )
        result = response.json()
        print(f"Response: {result}")
        status = result.get("status")
    
    # Step 3: Submit password if needed
    if status == "awaiting_password":
        print("\nStep 3: Enter your 2FA password")
        password = input("Enter password: ")
        
        password_data = {
            "session_id": session_id,
            "password": password
        }
        
        response = requests.post(
            f"{BASE_URL}/api/v1/telegram/submit-password",
            json=password_data
        )
        result = response.json()
        print(f"Response: {result}")
        status = result.get("status")
    
    # Check final status
    if status == "authorized":
        print("\n✅ Successfully authorized!")
        print(f"Session ID: {session_id}")
        
        # List sessions
        print("\nListing all sessions...")
        response = requests.get(f"{BASE_URL}/api/v1/telegram/sessions")
        sessions = response.json()
        print(f"Active sessions: {sessions}")
    else:
        print(f"\n❌ Authorization incomplete. Status: {status}")


def list_sessions_example():
    """Example of listing sessions."""
    print("Listing all sessions...")
    response = requests.get(f"{BASE_URL}/api/v1/telegram/sessions")
    
    if response.status_code == 200:
        sessions = response.json()
        print(f"Total sessions: {sessions.get('total')}")
        for session in sessions.get('sessions', []):
            print(f"  - {session}")
    else:
        print(f"Error: {response.status_code}")


def close_session_example(session_id: str):
    """Example of closing a session."""
    print(f"Closing session {session_id}...")
    response = requests.delete(f"{BASE_URL}/api/v1/telegram/sessions/{session_id}")
    
    if response.status_code == 200:
        result = response.json()
        print(f"✅ {result.get('message')}")
    else:
        print(f"❌ Error: {response.status_code}")


if __name__ == "__main__":
    print("=" * 50)
    print("Telegram Login API - Example Usage")
    print("=" * 50)
    print("\nMake sure the API server is running at", BASE_URL)
    print("Start the server with: python run.py\n")
    
    choice = input("Choose an option:\n1. Complete login flow\n2. List sessions\n3. Close a session\nEnter choice (1-3): ")
    
    if choice == "1":
        login_example()
    elif choice == "2":
        list_sessions_example()
    elif choice == "3":
        session_id = input("Enter session ID to close: ")
        close_session_example(session_id)
    else:
        print("Invalid choice")
