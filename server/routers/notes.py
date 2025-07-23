from fastapi import APIRouter, HTTPException, Depends, Query, Form, Path
from database import get_connection
from auth import get_current_user
import datetime

router = APIRouter()


@router.get("/notes/")
async def get_notes(
    skip: int = Query(0, ge=0),
    limit: int = Query(10, ge=1),
    current_user: dict = Depends(get_current_user)
):
    try:
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        user_id = current_user["id"]
        query = """
            SELECT * FROM notes
            WHERE user_id = %s
            ORDER BY created_at DESC
            LIMIT %s OFFSET %s
        """
        params = (user_id, limit, skip)
        cursor.execute(query, params)
        notes = cursor.fetchall()
        cursor.close()
        conn.close()
        return notes
    except Exception as e:
        print("Error in get:", e)
        return {"error": str(e)}


@router.post("/create_notes/")
async def create_notes(
    title: str = Form(...),
    note: str = Form(...),
    current_user: dict = Depends(get_current_user)
):
    try:
        user_id = current_user["id"]
        now = datetime.datetime.utcnow()
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        query = """
            SELECT COUNT(*) AS count
            FROM notes
            WHERE user_id = %s AND created_at >= %s
        """
        one_hour_ago = now - datetime.timedelta(hours=1)
        cursor.execute(query, (user_id, one_hour_ago))
        count = cursor.fetchone()
        if count["count"] >= 5:
            return {"message": "You have inserted 5 notes in the last hour already"}
        insert_query = """
            INSERT INTO notes (title, note, user_id, created_at)
            VALUES (%s, %s, %s, %s)
        """
        cursor.execute(insert_query, (title, note, user_id, now))
        note_id = cursor.lastrowid
        conn.commit()
        cursor.close()
        conn.close()
        return {"message": "Note posted", "note_id": note_id}
    except Exception as e:
        print("Error in create_notes:", e)
        return {"error": str(e)}
    
@router.put("/edit_notes/{id}/")
async def edit_notes(
    id: int = Path(...),
    title: str = Form(...),
    note: str = Form(...),
    current_user: dict = Depends(get_current_user)
):
    try:
        user_id = current_user["id"]
        now = datetime.datetime.utcnow()
        conn = get_connection()
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM notes WHERE id = %s AND user_id = %s", (id, user_id))
        existing = cursor.fetchone()
        if not existing:
            return {"message": "Note not found or not owned by user"}
        cursor.execute("""
            UPDATE notes
            SET title = %s, note = %s
            WHERE id = %s AND user_id = %s
        """, (title, note, id, user_id))
        conn.commit()
        cursor.close()
        conn.close()
        return {"message": "Note updated successfully", "note_id": id}
    
    except Exception as e:
        print("Error in edit_notes:", e)
        return {"error": str(e)}
@router.delete("/delete_notes/{id}/")
async def edit_notes(
    id: int = Path(...),
    current_user: dict = Depends(get_current_user)
):
    try:
         conn = get_connection()
         user_id = current_user["id"]
         cursor = conn.cursor(dictionary=True)
         cursor.execute("SELECT * FROM notes WHERE id = %s AND user_id = %s", (id, user_id))
         existing = cursor.fetchone()
         if not existing:
            return {"message": "Note not found or not owned by user"}
         cursor.execute("""
            DELETE FROM notes
            WHERE id = %s AND user_id = %s
        """, ( id, user_id))
         conn.commit()
         cursor.close()
         conn.close()
         return {"message": "Note updated successfully", "note_id": id}
    except Exception as e:
        print("Error in edit_notes:", e)
        return {"error": str(e)}


