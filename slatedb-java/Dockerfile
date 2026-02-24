# Stage 1: Build the native library with Rust
FROM rust:1.91.1-bookworm AS rust-builder

WORKDIR /repo
COPY . .
RUN cargo build -p slatedb-c --release --features slatedb/gcp

# Stage 2: Build (and optionally publish) the Java JAR
FROM eclipse-temurin:24-jdk-noble AS java-builder

RUN apt-get update -qq && \
    apt-get install -y -qq --no-install-recommends gcc libc6-dev && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /repo
COPY . .
COPY --from=rust-builder /repo/target/release/libslatedb_c.so target/release/libslatedb_c.so

WORKDIR /repo/slatedb-java
ENTRYPOINT ["./gradlew"]
CMD ["jar"]
