const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");

module.exports = {
  mode: "development",

  entry: "./src/index.tsx",

  output: {
    path: path.resolve(__dirname, "dist"),
    filename: "assets/js/[name].js",
    clean: true,
    publicPath: "/",
  },

  resolve: {
    extensions: [".tsx", ".ts", ".jsx", ".js"],
  },

  module: {
    rules: [
      {
        test: /\.tsx?$/,
        exclude: /node_modules/,
        use: {
          loader: "ts-loader",
        },
      },

      {
        test: /\.css$/,
        use: ["style-loader", "css-loader"],
      },

      {
        test: /\.(png|jpe?g|gif|svg|webp)$/i,
        type: "asset/resource",
      },

      {
        test: /\.(woff|woff2|eot|ttf|otf)$/i,
        type: "asset/resource",
      },
    ],
  },

  plugins: [
    new HtmlWebpackPlugin({
      template: path.resolve(__dirname, "public/index.html"),
    }),
  ],

  devServer: {
    port: 3000,

    hot: true,

    open: true,

    historyApiFallback: true,

    static: false,

    client: {
      overlay: true,
    },
  },

  devtool: "eval-source-map",

  optimization: {
    splitChunks: {
      chunks: "all",
    },
  },
};

