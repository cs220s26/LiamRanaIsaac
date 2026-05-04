# Watchlist Bot -  DevOps Final Project

## Test Status
![Testing](https://github.com/cs220s26/LiamRanaIsaac/actions/workflows/tests.yaml/badge.svg)

## Overview
This project demonstrates DevOps practices applied to a pre-existing Java Discord bot (Watchlist Bot) from CSCI 244. The bot itself allows Discord users to manage a personal watchlist of movies and TV shows through a conversational interface, but the focus of this project is the full DevOps pipeline surrounding it:

- Source control with **Git and GitHub** (within the cs220s26 organization)
- Build automation and static analysis with **Maven** (Checkstyle + fat JAR packaging)
- Secrets management via **AWS Secrets Manager** (replacing `.env` files)
- Database state management with **Redis** setup scripts
- Production deployment on **AWS EC2** managed by **SystemD**
- **CI** - automated testing and static analysis on every push via GitHub Actions
- **CD** - on demand automated redeployment to EC2 via GitHub Actions


## Dev Setup/Execution
### Prerequisites
- Java 21
- Maven
- Redis running locally on port 6379
- AWS credentials configured locally (`~/.aws/credentials`) with `secretsmanager:GetSecretValue` permission
- Discord bot token stored in AWS Secrets Manager (`us-east-1`)

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/cs220s26/LiamRanaIsaac.git
   cd LiamRanaIsaac
   ```

2. **Start Redis locally**
   ```bash
   # macOS (Homebrew)
   brew services start redis

   # Linux
   sudo systemctl start redis
   ```

3. **Build the project** — this runs Checkstyle and compiles, then packages a fat JAR
   ```bash
   mvn clean package
   ```

4. **Run the bot**
   ```bash
   java -jar target/WatchlistBot-1.0.0-jar-with-dependencies.jar
   ```

5. **(Optional) Reset Redis to a clean empty state**
   ```bash
   ./scripts/resetDB.sh
   ```

> **Note:** The bot loads its Discord token from AWS Secrets Manager (not a `.env` file). Local AWS credentials must be configured before running.

---


## Prod Setup/Execution
Production runs on an **AWS EC2** instance. The setup is fully automated via `scripts/userdata.sh`.

### Steps

1. **Launch an EC2 instance** (Amazon Linux 2, `t2.micro` or larger).
   - Attach an **IAM role** with `secretsmanager:GetSecretValue` permission.
   - Paste the contents of `scripts/userdata.sh` into the **User Data** field (Advanced Details).

   The `userdata.sh` script automatically:
   - Updates the system packages
   - Installs Git, Redis 6, Maven, and Amazon Corretto 21
   - Clones the repository to `/LiamRanaIsaac`
   - Starts and enables the Redis service
   - Builds the project (`mvn clean package`)
   - Copies `watchlistbot.service` to `/etc/systemd/system/`
   - Enables and starts the bot as a SystemD service

2. **Verify the service is running**
   ```bash
   sudo systemctl status watchlistbot
   ```

3. **To manually redeploy** (pull latest + rebuild + restart):
   ```bash
   ./scripts/redeploy.sh
   ```

4. **To reset Redis to a clean state:**
   ```bash
   ./scripts/resetDB.sh
   ```

---


## CI/CD Setup

### Workflow Files

Both workflows live in `.github/workflows/` and are already committed to the repo:

| File | Trigger | Purpose |
|---|---|---|
| `tests.yaml` | Every `git push` | Runs Checkstyle + JUnit tests |
| `redeploy.yaml` | Manual (`workflow_dispatch`) | SSH redeploy to EC2 |

### GitHub Secrets Configuration

The CD workflow authenticates to EC2 using an SSH key stored as a GitHub Secret.

1. Download your AWS Lab `.pem` private key file.
2. In your GitHub repository, go to **Settings → Secrets and variables → Actions → New repository secret**.
3. Name the secret **`LABSUSERPEM`** and paste the full contents of your `.pem` file as the value.

### CD Workflow — Host Configuration

Update the `host` field in `.github/workflows/redeploy.yaml` with your EC2 instance's public DNS or IP address:

```yaml
host: <your-ec2-public-dns-here>
```

---


## CI/CD Execution

### CI — Continuous Integration

**Trigger:** Automatically on every `git push` to any branch.

**What happens:**
1. GitHub Actions spins up an `ubuntu-latest` runner.
2. Java 21 (Amazon Corretto) is configured with Maven dependency caching.
3. `mvn test` runs, which:
   - Executes **Checkstyle** during the `validate` phase — the build fails on any style violation.
   - Compiles all source code.
   - Runs all **JUnit 5** unit tests.
4. A failing step marks the workflow as failed and blocks merging.

### CD — Continuous Deployment

**Trigger:** Manually — navigate to the **Actions** tab in GitHub, select `Redeploy on AWS`, and click **"Run workflow"**.

**What happens:**
1. GitHub Actions spins up an `ubuntu-latest` runner.
2. The runner SSHes into the EC2 instance using the `LABSUSERPEM` secret.
3. `scripts/redeploy.sh` runs on the EC2 instance:
   - `git pull origin main` — fetches the latest code.
   - `mvn clean package` — rebuilds the fat JAR with all dependencies.
   - `systemctl restart watchlistbot` — restarts the SystemD service with the new build.

---

## Technologies Used

| Technology | Purpose | Link |
|---|---|---|
| Java 21 (Amazon Corretto) | Primary application language | https://aws.amazon.com/corretto/ |
| Maven | Build automation, dependency management | https://maven.apache.org/ |
| Checkstyle | Static code analysis via Maven | https://checkstyle.sourceforge.io/ |
| JUnit 5 | Unit testing framework | https://junit.org/junit5/ |
| Git / GitHub | Version control and CI/CD hosting | https://github.com/ |
| GitHub Actions | CI/CD pipeline automation | https://docs.github.com/en/actions |
| AWS EC2 | Cloud production server | https://aws.amazon.com/ec2/ |
| AWS Secrets Manager | Secure token/credential storage | https://aws.amazon.com/secrets-manager/ |
| AWS SDK for Java v2 | Secrets Manager client library | https://aws.amazon.com/sdk-for-java/ |
| SystemD | Linux service manager for the bot process | https://systemd.io/ |
| Redis | Key-value database (production persistence) | https://redis.io/ |
| Jedis | Java client for Redis | https://github.com/redis/jedis |
| JDA (Java Discord API) | Discord bot framework (the application) | https://github.com/discord-jda/JDA |

---



## Background

Sources consulted while implementing the DevOps pipeline:

- **GitHub Actions — setup-java action** — https://github.com/actions/setup-java
- **GitHub Actions — appleboy/ssh-action (SSH deploy)** — https://github.com/appleboy/ssh-action
- **GitHub Actions — storing secrets** — https://docs.github.com/en/actions/security-for-github-actions/security-guides/using-secrets-in-github-actions
- **Maven Assembly Plugin (fat JAR packaging)** — https://maven.apache.org/plugins/maven-assembly-plugin/usage.html
- **Maven Checkstyle Plugin** — https://maven.apache.org/plugins/maven-checkstyle-plugin/usage.html
- **AWS Secrets Manager — retrieving secrets in Java** — https://docs.aws.amazon.com/secretsmanager/latest/userguide/retrieving-secrets_cache-java.html
- **SystemD service unit file reference** — https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html
- **EC2 User Data scripts** — https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/user-data.html
- **Redis — getting started** — https://redis.io/docs/latest/get-started/
- **Jedis — getting started** — https://github.com/redis/jedis/wiki/Getting-started

---


## Contributors:

* Liam Kerr
* Rana Yum
* Isaac Nunez
