# FlowCast FastAPI Mock Backend

This backend serves mock data for the first FlowCast Demo prototype.

## Run

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

Open:

```text
http://127.0.0.1:8000/docs
```
