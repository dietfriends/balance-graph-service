package com.balancefriends.graphql.bgs

enum class BgsOperator {
  EQ,
  GT,
  GTE,
  LT,
  LTE,
  NE,
  IN,
  NIN,
  CONTAINS,
  EXISTS;

  companion object {
    fun parse(name: String?): BgsOperator? {
      if (name == null) {
        return EQ
      }
      return BgsOperator.values().firstOrNull { it.name == name.toUpperCase() }
    }
  }
}