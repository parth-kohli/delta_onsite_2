from fastapi import FastAPI
from routers import users, notes
app = FastAPI()
app.include_router(users.router)
app.include_router(notes.router)