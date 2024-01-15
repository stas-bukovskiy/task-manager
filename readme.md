# task-manager (backend)

## Running

### Pre-requirements

- Docker
- Make

### Environment variables

In order to customize application configuration, you can use environment variables that are listed in the `.dev.env`
or `.docker.env` files

### Run in docker

To run the project with docker compose, you need to run the following command:

```bash
make prod-up
```

To stop the project, you need to run the following command:

```bash
make prod-down
```

### Run with IDE or gradle

To run the project in a local environment, you need to run the following command.

```bash
make local-up
```

After that, you can run with IDE or grade command:

```bash
male local-run
```

To stop the project, you need to run the following command:

```bash
make local-down
```