FROM node:20-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . .

ENV NODE_ENV=development
ENV REACT_APP_API_URL=http://localhost:8080

EXPOSE 3000
CMD ["npm", "start"]
