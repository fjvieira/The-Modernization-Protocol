import importlib
import logging
import pkgutil
from collections.abc import Awaitable, Callable
from datetime import UTC, datetime
from uuid import uuid4

from fastapi import FastAPI, Request, status
from fastapi.responses import JSONResponse
from starlette.responses import Response

import src.routers as routers_pkg

logger = logging.getLogger("uvicorn.error")

app = FastAPI(title="Shipping Service API", redirect_slashes=True)


def include_all_routers(app: FastAPI) -> None:
    """Dynamically imports and mounts all routers found in src/routers."""
    package_path = routers_pkg.__path__
    package_name = routers_pkg.__name__

    for _, module_name, _ in pkgutil.iter_modules(package_path):
        # Import each module dynamically (e.g., src.routers.zones)
        module = importlib.import_module(f"{package_name}.{module_name}")

        # Check if the module contains a FastAPI router object
        if hasattr(module, "router"):
            app.include_router(module.router)


# Mount all routers automatically
include_all_routers(app)
