docker run --name assets-db \
-p 5432:5432 \
-e POSTGRES_PASSWORD=assets \
-e POSTGRES_USER=assets \
-e POSTGRES_DB=assets \
-d postgres