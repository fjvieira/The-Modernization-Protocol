from fastapi import APIRouter

router = APIRouter(prefix="/dummy")


@router.get("/")
async def dummy_endpoint() -> dict[str, str]:
    return {"message": "This is a dummy response"}
