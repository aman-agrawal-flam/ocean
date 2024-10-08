/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

#ifndef META_OCEAN_PLATFORM_META_QUEST_QUEST_H
#define META_OCEAN_PLATFORM_META_QUEST_QUEST_H

#include "ocean/platform/meta/Meta.h"

namespace Ocean
{

namespace Platform
{

namespace Meta
{

namespace Quest
{

/**
 * @ingroup platformmeta
 * @defgroup platformmetaquest Ocean Platform Meta Quest Library
 * @{
 * The Ocean Meta Quest Library provides specific functionalities for Meta's Quest platform.
 * The library is available on Meta Quest platforms only.
 * @}
 */

/**
 * @namespace Ocean::Platform::Meta::Quest Namespace of the Platform Meta Quest library.<p>
 * The Namespace Ocean::Platform::Meta::Quest is used in the entire Ocean Platform Meta Quest Library.
 */

// Defines OCEAN_PLATFORM_META_QUEST_EXPORT for dll export and import.
#if defined(_WINDOWS) && defined(OCEAN_RUNTIME_SHARED)
	#ifdef USE_OCEAN_PLATFORM_META_QUEST_EXPORT
		#define OCEAN_PLATFORM_META_QUEST_EXPORT __declspec(dllexport)
	#else
		#define OCEAN_PLATFORM_META_QUEST_EXPORT __declspec(dllimport)
	#endif
#else
	#define OCEAN_PLATFORM_META_QUEST_EXPORT
#endif

}

}

}

}

#endif // META_OCEAN_PLATFORM_META_QUEST_QUEST_H
