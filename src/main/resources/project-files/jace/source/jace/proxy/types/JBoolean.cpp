
#include "jace/proxy/types/JBoolean.h"

#ifndef JACE_JCLASS_IMPL_H
#include "jace/JClassImpl.h"
#endif
using jace::JClassImpl;

#include "jace/BoostWarningOff.h"
#include <boost/thread/mutex.hpp>
#include "jace/BoostWarningOn.h"

BEGIN_NAMESPACE_3( jace, proxy, types )

JBoolean::JBoolean( jvalue value )
{
  setJavaJniValue( value );
}

JBoolean::JBoolean( jboolean boolean_ )
{
  jvalue value;
  value.z = boolean_;
  setJavaJniValue( value );
}

JBoolean::~JBoolean()
{}

JBoolean::operator jboolean() const
{ 
  return getJavaJniValue().z;
}

jboolean JBoolean::getBoolean() const
{
  return getJavaJniValue().z;
}

bool JBoolean::operator==( const JBoolean& boolean_ ) const
{
  return boolean_.getBoolean() == getBoolean();
}

bool JBoolean::operator!=( const JBoolean& boolean_ ) const
{
  return !( *this == boolean_ );
}

bool JBoolean::operator==( jboolean val ) const
{
  return val == getBoolean();
}

bool JBoolean::operator!=( jboolean val ) const
{
  return ! ( *this == val );
}

static boost::mutex javaClassMutex;
const JClass& JBoolean::staticGetJavaJniClass() throw ( JNIException )
{
	static boost::shared_ptr<JClassImpl> result;
	boost::mutex::scoped_lock lock(javaClassMutex);
	if (result == 0)
		result = boost::shared_ptr<JClassImpl>(new JClassImpl("boolean", "Z"));
	return *result;
}

const JClass& JBoolean::getJavaJniClass() const throw ( JNIException )
{
  return JBoolean::staticGetJavaJniClass();
}

END_NAMESPACE_3( jace, proxy, types )
