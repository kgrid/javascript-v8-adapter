function bmicalc(inputs){
  height = inputs.height;
  weight = inputs.weight;
  return Number.parseFloat(weight/Math.pow(height, 2)).toFixed(1);
}

