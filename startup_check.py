"""Startup checks for the Telegram Login API."""

import sys


def check_telegram_dependencies():
    """Check if telegram client dependencies are available."""
    try:
        from telegram.client import Telegram
        return True, None
    except ImportError as e:
        return False, (
            "Failed to import telegram.client. "
            "Please install: pip install python-telegram\n"
            f"Error: {e}"
        )
    except Exception as e:
        return False, (
            "Error importing telegram.client. "
            "TDLib might not be properly installed or configured.\n"
            f"Error: {e}"
        )


def run_startup_checks():
    """Run all startup checks."""
    checks = [
        ("Telegram Dependencies", check_telegram_dependencies),
    ]
    
    all_passed = True
    for name, check_func in checks:
        passed, error_msg = check_func()
        if not passed:
            print(f"❌ {name} check failed:", file=sys.stderr)
            print(f"   {error_msg}", file=sys.stderr)
            all_passed = False
        else:
            print(f"✅ {name} check passed")
    
    return all_passed


if __name__ == "__main__":
    if run_startup_checks():
        print("\n✅ All startup checks passed!")
        sys.exit(0)
    else:
        print("\n❌ Some startup checks failed. Please fix the issues above.", file=sys.stderr)
        sys.exit(1)
