package rules

import com.typesafe.config.ConfigValue
import framework.config.SystemConfiguration


class ConfigurationInjector {

  def inject(configs: Set[(String, ConfigValue)]) = {
    configs.map {
      config =>
        SystemConfiguration.current.addValue(config._1, config._2.unwrapped())
    }
  }

}
