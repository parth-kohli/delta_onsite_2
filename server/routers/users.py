from fastapi import APIRouter, HTTPException, Form, Depends
from database import get_connection
from auth import hash_password, verify_password, create_token, get_current_user
router = APIRouter()
@router.post("/create_user/")
def create_user(username: str = Form(...), password: str = Form(...)):
    conn = get_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute("SELECT COUNT(*) as count FROM users WHERE username = %s", (username,))
    count = cursor.fetchone()

    if count["count"] > 0:
        raise HTTPException(status_code=400, detail="User already exists with this username")

    hashed_password = hash_password(password)
    cursor.execute("INSERT INTO users (username, password) VALUES (%s, %s)", (username, hashed_password))
    conn.commit()

    cursor.close()
    conn.close()

    return {"message": "User created"}
@router.post("/login/")
def login(username: str = Form(...), password: str = Form(...)):
    conn = get_connection()
    cursor = conn.cursor(dictionary=True)
    print(username)
    print(password)
    cursor.execute("SELECT * FROM users WHERE username = %s", (username,))
    user = cursor.fetchone()
    if not user or not verify_password(password, user["password"]):
        raise HTTPException(status_code=400, detail="Invalid username or password")

    payload = {
        "user_id": user["id"],
        "username": user["username"]
    }
    token = create_token(payload)

    cursor.close()
    conn.close()

    return {
        "access_token": token,
        "token_type": "bearer",
        "user_id": user["id"],
        "username": user["username"]
    }
@router.post("/loginwithtoken")
def loginwithtoken(current_user: dict = Depends(get_current_user)):
    user_id = current_user["id"]
    return {"id": current_user["id"], "username": current_user["username"] }