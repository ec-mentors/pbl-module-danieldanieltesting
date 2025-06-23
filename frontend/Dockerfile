# --- STAGE 1: Build Stage ---
# Use an official Node.js LTS (Long-Term Support) image as the builder.
FROM node:20-alpine AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy package.json and package-lock.json (or yarn.lock, pnpm-lock.yaml)
# This leverages Docker's layer caching to avoid re-installing dependencies if they haven't changed.
COPY package*.json ./

# Install project dependencies
RUN npm install

# Copy the rest of the application's source code
COPY . .

# Build the application for production.
# This will create a 'dist' folder with the static assets.
RUN npm run build


# --- STAGE 2: Runtime Stage ---
# Use the official Nginx image. It's lightweight and optimized for serving static files.
FROM nginx:1.27-alpine

# Remove the default Nginx welcome page.
RUN rm /etc/nginx/conf.d/default.conf

# Copy our custom Nginx configuration file.
COPY nginx.conf /etc/nginx/conf.d

# Copy the built static assets from the 'builder' stage into the Nginx public HTML directory.
COPY --from=builder /app/dist /usr/share/nginx/html

# Expose port 80, which is the default port Nginx listens on.
EXPOSE 80

# The command to start the Nginx server when the container starts.
ENTRYPOINT ["nginx", "-g", "daemon off;"]