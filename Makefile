GRADLEW           ?= ./gradlew

local-run:
	$(GRADLEW) bootRun

prod-up:
	@chmod +x run.sh
	@./run.sh

prod-down:
	docker compose -f compose-prod.yaml down

local-up:
	docker compose -f compose-local.yaml up -d  --remove-orphans

local-down:
	docker compose -f compose-local.yaml down